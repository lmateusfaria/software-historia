package br.com.unifef.biblioteca.services;

import br.com.unifef.biblioteca.domains.Documento;
import br.com.unifef.biblioteca.domains.Usuario;
import br.com.unifef.biblioteca.domains.graph.DocumentoNode;
import br.com.unifef.biblioteca.repositories.graph.DocumentoNodeRepository;
import br.com.unifef.biblioteca.domains.dtos.DocumentoDTO;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import br.com.unifef.biblioteca.repositories.DocumentoRepository;
import br.com.unifef.biblioteca.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.springframework.mock.web.MockMultipartFile;

@Slf4j
@Service
public class DocumentoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DocumentoRepository repository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private DocumentoNodeRepository documentoNodeRepository;

    @Transactional(readOnly = true)
    public List<DocumentoDTO> findAll() {
        return repository.findAll().stream().map(DocumentoDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<DocumentoDTO> findByStatus(StatusDocumento status) {
        return repository.findByStatus(status).stream().map(DocumentoDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public DocumentoDTO findById(Long id) {
        log.info("Buscando documento por ID: {}", id);
        
        // Busca o documento no PostgreSQL
        Documento doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        
        // Força o carregamento da coleção ElementCollection (Lazy) para evitar LazyInitializationException no DTO
        if (doc.getImagensUrls() != null) {
            doc.getImagensUrls().size(); 
        }

        DocumentoNode node = null;
        try {
            log.info("Buscando nó correspondente no Neo4j para o ID: {}", id);
            // Chamada ao Neo4j - O repositório agora sabe qual manager usar
            node = documentoNodeRepository.findById(id).orElse(null);
            if (node != null) {
                log.info("Nó encontrado no Neo4j: {}", node.getTitulo());
            } else {
                log.info("Nenhum nó encontrado no Neo4j para o ID: {}", id);
            }
        } catch (Exception e) {
            log.error("Houve uma falha ao acessar o Neo4j para o documento {}. Causa: {}", id, e.getMessage());
            log.warn("O sistema seguirá servindo o documento básico do PostgreSQL.");
        }
        
        return new DocumentoDTO(doc, node);
    }

    @Transactional(transactionManager = "transactionManager")
    public DocumentoDTO create(DocumentoDTO dto, List<MultipartFile> files) {
        Documento doc = new Documento();
        doc.setDescricao(dto.getDescricao());
        doc.setStatus(StatusDocumento.PENDENTE_OCR);
        doc.setDataDigitalizacao(LocalDate.now());
        doc.setTipo(dto.getTipo());
        doc.setDiaDocumento(dto.getDiaDocumento());
        doc.setMesDocumento(dto.getMesDocumento());
        doc.setAnoDocumento(dto.getAnoDocumento());
        doc.setLocalOrigem(dto.getLocalOrigem());
        doc.setEdicao(dto.getEdicao());
        doc.setMarcadores(dto.getMarcadores());
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        doc.setResponsavel(usuario);

        if (files != null && !files.isEmpty()) {
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file.getContentType() != null && file.getContentType().equalsIgnoreCase("application/pdf")) {
                    try (PDDocument pdfDocument = Loader.loadPDF(file.getBytes())) {
                        PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
                        for (int page = 0; page < pdfDocument.getNumberOfPages(); ++page) {
                            BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(bim, "jpg", baos);
                            baos.flush();
                            byte[] imageInByte = baos.toByteArray();
                            baos.close();
                            
                            String genName = file.getOriginalFilename() != null ? 
                                   file.getOriginalFilename().replace(".pdf", "") + "_pag_" + (page + 1) + ".jpg" : 
                                   "pag_" + (page + 1) + ".jpg";

                            MultipartFile pageFile = new MockMultipartFile(
                                genName, genName, "image/jpeg", new ByteArrayInputStream(imageInByte)
                            );
                            urls.add(fileStorageService.save(pageFile));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao processar as páginas do PDF", e);
                    }
                } else {
                    urls.add(fileStorageService.save(file));
                }
            }
            doc.setImagensUrls(urls);
        }

        Documento savedDoc = repository.save(doc);

        // Sincronização com Neo4j (Grafo)
        try {
            log.info("Sincronizando novo documento com Neo4j. ID: {}", savedDoc.getId());
            DocumentoNode node = new DocumentoNode(savedDoc.getId(), savedDoc.getEdicao() != null ? savedDoc.getEdicao() : savedDoc.getTipo());
            documentoNodeRepository.save(node);
            log.info("Documento {} sincronizado com sucesso no Neo4j.", savedDoc.getId());
        } catch (Exception e) {
            log.error("Falha ao sincronizar documento {} com Neo4j: {}", savedDoc.getId(), e.getMessage());
            // O processo continua pois o Postgres é a fonte primária de verdade
        }

        return new DocumentoDTO(savedDoc);
    }

    @Transactional(transactionManager = "transactionManager")
    public void delete(Long id) {
        Documento doc = findById(id);
        
        // 1. Remover arquivos do MinIO
        if (doc.getImagensUrls() != null) {
            for (String filename : doc.getImagensUrls()) {
                try {
                    fileStorageService.delete(filename);
                } catch (Exception e) {
                    System.err.println("Erro ao deletar arquivo no MinIO: " + filename + ". Erro: " + e.getMessage());
                }
            }
        }

        // 2. Remover do Neo4j (resiliente)
        try {
            documentoNodeRepository.deleteById(id);
        } catch (Exception e) {
            System.err.println("Erro ao deletar nó no Neo4j para documento " + id + ". Erro: " + e.getMessage());
        }

        // 3. Remover do Postgres
        documentoRepo.delete(doc);
    }

    @Transactional(transactionManager = "transactionManager")
    public DocumentoDTO approve(Long id) {
        Documento doc = findById(id);
        doc.setStatus(StatusDocumento.APROVADO);
        doc = documentoRepo.save(doc);
        return new DocumentoDTO(doc);
    }

    public InputStream getFileStream(String filename) {
        return fileStorageService.fetch(filename);
    }
}
