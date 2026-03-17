package br.com.unifef.biblioteca.services;

import br.com.unifef.biblioteca.domains.Documento;
import br.com.unifef.biblioteca.domains.Usuario;
import br.com.unifef.biblioteca.domains.graph.DocumentoNode;
import br.com.unifef.biblioteca.repositories.graph.DocumentoNodeRepository;
import br.com.unifef.biblioteca.domains.dtos.DocumentoDTO;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import br.com.unifef.biblioteca.repositories.DocumentoRepository;
import br.com.unifef.biblioteca.repositories.UsuarioRepository;
import br.com.unifef.biblioteca.domains.ImagemOcrResultado;
import br.com.unifef.biblioteca.repositories.ImagemOcrRepository;
import br.com.unifef.biblioteca.domains.graph.ImagemNode;
import br.com.unifef.biblioteca.repositories.graph.ImagemNodeRepository;
import br.com.unifef.biblioteca.repositories.graph.PessoaRepository;
import br.com.unifef.biblioteca.repositories.graph.LocalRepository;
import br.com.unifef.biblioteca.repositories.graph.EventoRepository;
import br.com.unifef.biblioteca.repositories.graph.OrganizacaoRepository;
import br.com.unifef.biblioteca.repositories.graph.AssuntoRepository;
import br.com.unifef.biblioteca.domains.dtos.OcrResultadoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.springframework.mock.web.MockMultipartFile;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.Arrays;
import java.util.Comparator;

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

    @Autowired
    private GptOcrService gptOcrService;

    @Autowired
    private ImagemOcrRepository imagemOcrRepository;

    @Autowired
    private ImagemNodeRepository imagemNodeRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private LocalRepository localRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @Autowired
    private AssuntoRepository assuntoRepository;

    // Executor para processamento geral (OCR, sincronização, etc)
    private final ExecutorService generalExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    
    // Executor dedicado para processamento de PDF (limitado para preservar RAM)
    private final ExecutorService pdfExecutor = Executors.newFixedThreadPool(4);

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
        
        DocumentoDTO dto = new DocumentoDTO(doc, node);
        
        // Carregar resultados de OCR persistidos para cada imagem
        List<ImagemOcrResultado> ocrResultados = imagemOcrRepository.findByDocumentoIdOrderByIndice(id);
        if (ocrResultados != null && !ocrResultados.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, OcrResultadoDTO> ocrMap = new HashMap<>();
            
            for (ImagemOcrResultado res : ocrResultados) {
                try {
                    OcrResultadoDTO ocrDto = new OcrResultadoDTO();
                    ocrDto.setTextoCompleto(res.getTextoExtraido());
                    ocrDto.setTipoDocumento(res.getTipoDocumento());
                    
                    if (res.getPessoasExtraidas() != null) ocrDto.setPessoas(mapper.readValue(res.getPessoasExtraidas(), new TypeReference<List<String>>(){}));
                    if (res.getLocaisExtraidos() != null) ocrDto.setLocais(mapper.readValue(res.getLocaisExtraidos(), new TypeReference<List<String>>(){}));
                    if (res.getEventosExtraidos() != null) ocrDto.setEventos(mapper.readValue(res.getEventosExtraidos(), new TypeReference<List<String>>(){}));
                    if (res.getOrganizacoesExtraidas() != null) ocrDto.setOrganizacoes(mapper.readValue(res.getOrganizacoesExtraidas(), new TypeReference<List<String>>(){}));
                    if (res.getAssuntosExtraidos() != null) ocrDto.setAssuntos(mapper.readValue(res.getAssuntosExtraidos(), new TypeReference<List<String>>(){}));
                    if (res.getDatasMencionadas() != null) ocrDto.setDatasMencionadas(mapper.readValue(res.getDatasMencionadas(), new TypeReference<List<String>>(){}));
                    
                    // A chave deve ser a URL formatada para o download, para coincidir com o frontend
                    String fullUrl = "/api/documentos/download/" + res.getImagemUrl();
                    ocrMap.put(fullUrl, ocrDto);
                } catch (Exception e) {
                    log.error("Erro ao converter OCR persistido para DTO no documento {}", id, e);
                }
            }
            dto.setOcrResultadosImagem(ocrMap);
        }
        
        return dto;
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
            List<CompletableFuture<List<String>>> futures = new ArrayList<>();

            for (MultipartFile file : files) {
                String originalFilename = file.getOriginalFilename();
                String contentType = file.getContentType();

                if (contentType != null && contentType.equalsIgnoreCase("application/pdf")) {
                    futures.add(processPdf(file));
                } else if (originalFilename != null && originalFilename.toLowerCase().endsWith(".heic")) {
                    futures.add(CompletableFuture.supplyAsync(() -> 
                        Collections.singletonList(processHeic(file)), generalExecutor));
                } else {
                    futures.add(CompletableFuture.supplyAsync(() -> 
                        Collections.singletonList(fileStorageService.save(file)), generalExecutor));
                }
            }

            List<String> urls = new ArrayList<>();
            for (CompletableFuture<List<String>> future : futures) {
                urls.addAll(future.join());
            }
            doc.setImagensUrls(urls);
            
            // Gerar thumbnail da primeira imagem se disponível
            if (!urls.isEmpty()) {
                try (InputStream is = fileStorageService.fetch(urls.get(0))) {
                    byte[] imageBytes = is.readAllBytes();
                    doc.setUrlThumbnail(gerarThumbnail(imageBytes, urls.get(0)));
                } catch (Exception e) {
                    log.warn("Falha ao gerar thumbnail para novo documento: {}", e.getMessage());
                }
            }
        }

        // Suporte para arquivos pré-upados via chunked upload
        if (dto.getPreUploadedFiles() != null && !dto.getPreUploadedFiles().isEmpty()) {
            List<String> existingUrls = doc.getImagensUrls() != null ? doc.getImagensUrls() : new ArrayList<>();
            List<CompletableFuture<List<String>>> futures = new ArrayList<>();
            List<File> filesToDelete = new ArrayList<>();

            for (String filePath : dto.getPreUploadedFiles()) {
                File file = new File(filePath);
                if (file.exists()) {
                    try {
                        filesToDelete.add(file);
                        String originalFilename = file.getName();
                        String contentType = Files.probeContentType(file.toPath());
                        
                        if (contentType != null && contentType.equalsIgnoreCase("application/pdf")) {
                            futures.add(processPdfFromDisk(file));
                        } else if (originalFilename != null && originalFilename.toLowerCase().endsWith(".heic")) {
                            futures.add(CompletableFuture.supplyAsync(() -> 
                                Collections.singletonList(processHeicFromDisk(file)), generalExecutor));
                        } else {
                            futures.add(CompletableFuture.supplyAsync(() -> {
                                try (InputStream is = new java.io.FileInputStream(file)) {
                                    MultipartFile multipartFile = new MockMultipartFile(
                                        originalFilename, originalFilename, contentType, is
                                    );
                                    return Collections.singletonList(fileStorageService.save(multipartFile));
                                } catch (Exception e) {
                                    throw new RuntimeException("Erro ao processar arquivo regular do disco", e);
                                }
                            }, generalExecutor));
                        }
                    } catch (Exception e) {
                        log.error("Erro ao preparar arquivo pré-upado no disco: {}", filePath, e);
                    }
                }
            }

            for (CompletableFuture<List<String>> future : futures) {
                existingUrls.addAll(future.join());
            }

            // Limpar arquivos temporários mesclados SOMENTE após o processamento completo
            for (File f : filesToDelete) {
                try {
                    Files.deleteIfExists(f.toPath());
                    // Também tenta remover a pasta pai (o ID do upload) se estiver vazia
                    Files.deleteIfExists(f.getParentFile().toPath());
                } catch (Exception e) {
                    log.warn("Erro ao limpar arquivo ou pasta temporária: {}", f.getAbsolutePath());
                }
            }
            
            doc.setImagensUrls(existingUrls);

            // Gerar thumbnail da primeira imagem do chunked upload
            if (doc.getUrlThumbnail() == null && !existingUrls.isEmpty()) {
                try (InputStream is = fileStorageService.fetch(existingUrls.get(0))) {
                    byte[] imageBytes = is.readAllBytes();
                    doc.setUrlThumbnail(gerarThumbnail(imageBytes, existingUrls.get(0)));
                } catch (Exception e) {
                    log.warn("Falha ao gerar thumbnail para chunked upload: {}", e.getMessage());
                }
            }
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

    private CompletableFuture<List<String>> processPdf(MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            Path tempFile = null;
            try {
                tempFile = Files.createTempFile("pdf-upload-", ".pdf");
                file.transferTo(tempFile.toFile());
                return processPdfInternal(tempFile.toFile(), file.getOriginalFilename());
            } catch (Exception e) {
                log.error("Erro ao carregar ou processar PDF", e);
                throw new RuntimeException("Erro ao processar PDF", e);
            } finally {
                if (tempFile != null) {
                    try { Files.deleteIfExists(tempFile); } catch (Exception ex) { log.warn("Erro ao deletar PDF temporário", ex); }
                }
            }
        }, generalExecutor);
    }

    private CompletableFuture<List<String>> processPdfFromDisk(File file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return processPdfInternal(file, file.getName());
            } catch (Exception e) {
                log.error("Erro ao processar PDF do disco", e);
                throw new RuntimeException("Erro ao processar PDF do disco", e);
            }
        }, generalExecutor);
    }

    private List<String> processPdfInternal(File file, String originalFilename) throws Exception {
        try (PDDocument pdfDocument = Loader.loadPDF(new org.apache.pdfbox.io.RandomAccessReadBufferedFile(file))) {
            PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
            List<CompletableFuture<String>> pageFutures = new ArrayList<>();

            for (int page = 0; page < pdfDocument.getNumberOfPages(); ++page) {
                final int pageNum = page;
                pageFutures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        BufferedImage bim = pdfRenderer.renderImageWithDPI(pageNum, 200, ImageType.RGB);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(bim, "jpg", baos);
                        baos.flush();
                        byte[] imageInByte = baos.toByteArray();
                        
                        String genName = (originalFilename != null ? 
                               originalFilename.replaceAll("(?i)\\.pdf$", "") : "doc") + 
                               "_pag_" + (pageNum + 1) + ".jpg";

                        MultipartFile pageFile = new MockMultipartFile(
                            genName, genName, "image/jpeg", new ByteArrayInputStream(imageInByte)
                        );
                        return fileStorageService.save(pageFile);
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao processar página " + pageNum + " do PDF", e);
                    }
                }, pdfExecutor)); // Uso do executor limitado
            }

            return pageFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        }
    }

    private String processHeic(MultipartFile file) {
        Path tempHeic = null;
        Path tempJpg = null;
        try {
            tempHeic = Files.createTempFile("upload-", ".heic");
            file.transferTo(tempHeic.toFile());

            tempJpg = tempHeic.resolveSibling(tempHeic.getFileName().toString().replace(".heic", ".jpg"));

            log.info("Iniciando conversão HEIC para JPG: {}", tempHeic);
            ProcessBuilder pb = new ProcessBuilder("heif-convert", tempHeic.toString(), tempJpg.toString());
            Process process = pb.start();
            int exitCode = process.waitFor();
            log.info("Conversão HEIC finalizada. Exit code: {}", exitCode);

            if (exitCode != 0) {
                throw new RuntimeException("Falha na conversão HEIC. Código de saída: " + exitCode);
            }

            String genName = (file.getOriginalFilename() != null ? 
                    file.getOriginalFilename().replaceAll("(?i)\\.heic$", "") : "image") + ".jpg";
            
            try (InputStream is = Files.newInputStream(tempJpg)) {
                MultipartFile convertedFile = new MockMultipartFile(
                    genName, genName, "image/jpeg", is
                );
                return fileStorageService.save(convertedFile);
            }

        } catch (Exception e) {
            log.error("Erro ao converter HEIC para JPG", e);
            throw new RuntimeException("Erro ao processar imagem HEIC", e);
        } finally {
            try {
                if (tempHeic != null) Files.deleteIfExists(tempHeic);
                if (tempJpg != null) Files.deleteIfExists(tempJpg);
            } catch (Exception ex) {
                log.warn("Erro ao limpar arquivos temporários de conversão HEIC", ex);
            }
        }
    }

    private String processHeicFromDisk(File file) {
        Path tempJpg = null;
        try {
            Path tempHeic = file.toPath();
            tempJpg = tempHeic.resolveSibling(tempHeic.getFileName().toString().replace(".heic", ".jpg"));

            log.info("Iniciando conversão HEIC do disco para JPG: {}", tempHeic);
            ProcessBuilder pb = new ProcessBuilder("heif-convert", tempHeic.toString(), tempJpg.toString());
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) throw new RuntimeException("Falha na conversão HEIC. Código: " + exitCode);

            String genName = file.getName().replaceAll("(?i)\\.heic$", "") + ".jpg";
            try (InputStream is = Files.newInputStream(tempJpg)) {
                MultipartFile convertedFile = new MockMultipartFile(genName, genName, "image/jpeg", is);
                return fileStorageService.save(convertedFile);
            }
        } catch (Exception e) {
            log.error("Erro ao converter HEIC do disco", e);
            throw new RuntimeException("Erro ao processar HEIC do disco", e);
        } finally {
            if (tempJpg != null) try { Files.deleteIfExists(tempJpg); } catch (Exception ex) {}
        }
    }

    public String handleChunk(MultipartFile chunk, String uploadId, int chunkIndex, int totalChunks, String filename) {
        try {
            Path uploadDir = Paths.get("uploads", "tmp", uploadId);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path chunkPath = uploadDir.resolve(String.format("%05d", chunkIndex));
            Files.copy(chunk.getInputStream(), chunkPath);

            if (chunkIndex == totalChunks - 1) {
                return mergeChunks(uploadId, totalChunks, filename);
            }

            return null; // Ainda aguardando mais chunks
        } catch (Exception e) {
            log.error("Erro ao processar chunk {} de {}: {}", chunkIndex, uploadId, e.getMessage());
            throw new RuntimeException("Erro ao processar chunk de upload", e);
        }
    }

    private String mergeChunks(String uploadId, int totalChunks, String filename) {
        try {
            Path uploadDir = Paths.get("uploads", "tmp", uploadId);
            Path mergedFilePath = uploadDir.resolve(filename);

            try (OutputStream os = new FileOutputStream(mergedFilePath.toFile())) {
                File[] chunks = uploadDir.toFile().listFiles((dir, name) -> name.matches("\\d{5}"));
                if (chunks == null || chunks.length != totalChunks) {
                    throw new RuntimeException("Fragmentos de arquivo incompletos ou ausentes");
                }

                Arrays.sort(chunks, Comparator.comparing(File::getName));

                for (File chunk : chunks) {
                    Files.copy(chunk.toPath(), os);
                    Files.delete(chunk.toPath());
                }
            }

            log.info("Arquivo {} mesclado com sucesso em {}", filename, mergedFilePath);
            return mergedFilePath.toAbsolutePath().toString();
        } catch (Exception e) {
            log.error("Erro ao mesclar chunks para upload {}: {}", uploadId, e.getMessage());
            throw new RuntimeException("Erro ao mesclar fragmentos de arquivo", e);
        }
    }


    @Transactional(transactionManager = "transactionManager")
    public void delete(Long id) {
        Documento doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        
        // 1. Remover arquivos do MinIO
        if (doc.getImagensUrls() != null) {
            for (String filename : doc.getImagensUrls()) {
                try {
                    fileStorageService.delete(filename);
                } catch (Exception e) {
                    log.error("Erro ao deletar arquivo no MinIO: {}. Erro: {}", filename, e.getMessage());
                }
            }
        }

        // 2. Remover do Neo4j (resiliente)
        try {
            documentoNodeRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Erro ao deletar nó no Neo4j para documento {}. Erro: {}", id, e.getMessage());
        }

        // 3. Remover do Postgres
        repository.delete(doc);
    }

    @Transactional(transactionManager = "transactionManager")
    public DocumentoDTO approve(Long id) {
        Documento doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        doc.setStatus(StatusDocumento.APROVADO);
        doc = repository.save(doc);
        return new DocumentoDTO(doc);
    }

    public InputStream getFileStream(String filename) {
        return fileStorageService.fetch(filename);
    }

    @Transactional(transactionManager = "transactionManager")
    public OcrResultadoDTO processarOcrImagem(Long documentoId, String imagemUrl) {
        Documento doc = repository.findById(documentoId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

        Integer indice = null;
        if (doc.getImagensUrls() != null) {
            for (int i = 0; i < doc.getImagensUrls().size(); i++) {
                if (doc.getImagensUrls().get(i).equals(imagemUrl)) {
                    indice = i;
                    break;
                }
            }
        }
        
        if (indice == null) {
            throw new RuntimeException("Imagem não pertence a este documento");
        }

        try {
            // 1. Obter imagem do MinIO
            byte[] imageBytes;
            try (InputStream is = fileStorageService.fetch(imagemUrl)) {
                imageBytes = is.readAllBytes();
            }
            
            String mimeType = "image/jpeg";
            if (imagemUrl.toLowerCase().endsWith(".png")) mimeType = "image/png";

            // 2. Processar OCR GPT-4o-mini
            OcrResultadoDTO ocrResult = gptOcrService.extrairDadosImagem(imageBytes, mimeType);

            // 3. Salvar no Postgres
            ImagemOcrResultado entity = new ImagemOcrResultado();
            entity.setDocumentoId(documentoId);
            entity.setImagemUrl(imagemUrl);
            entity.setIndice(indice);
            entity.setTextoExtraido(ocrResult.getTextoCompleto());
            
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                entity.setPessoasExtraidas(mapper.writeValueAsString(ocrResult.getPessoas()));
                entity.setLocaisExtraidos(mapper.writeValueAsString(ocrResult.getLocais()));
                entity.setEventosExtraidos(mapper.writeValueAsString(ocrResult.getEventos()));
                entity.setOrganizacoesExtraidas(mapper.writeValueAsString(ocrResult.getOrganizacoes()));
                entity.setAssuntosExtraidos(mapper.writeValueAsString(ocrResult.getAssuntos()));
                entity.setDatasMencionadas(mapper.writeValueAsString(ocrResult.getDatasMencionadas()));
            } catch (Exception e) {
                log.error("Erro ao serializar arrays do OCR", e);
            }
            
            entity.setTipoDocumento(ocrResult.getTipoDocumento());
            imagemOcrRepository.save(entity);

            // 4. Salvar no Neo4J
            try {
                ImagemNode imagemNode = new ImagemNode(documentoId, imagemUrl, indice);
                imagemNode.setTextoExtraido(ocrResult.getTextoCompleto());
                
                for (String nome : ocrResult.getPessoas()) {
                    if (nome != null && !nome.trim().isEmpty()) {
                        br.com.unifef.biblioteca.domains.graph.Pessoa p = pessoaRepository.findById(nome)
                            .orElse(new br.com.unifef.biblioteca.domains.graph.Pessoa(nome));
                        pessoaRepository.save(p);
                        imagemNode.getPessoas().add(p);
                    }
                }
                
                for (String local : ocrResult.getLocais()) {
                    if (local != null && !local.trim().isEmpty()) {
                        br.com.unifef.biblioteca.domains.graph.Local l = localRepository.findById(local)
                            .orElse(new br.com.unifef.biblioteca.domains.graph.Local(local));
                        localRepository.save(l);
                        imagemNode.getLocais().add(l);
                    }
                }

                for (String evento : ocrResult.getEventos()) {
                    if (evento != null && !evento.trim().isEmpty()) {
                        br.com.unifef.biblioteca.domains.graph.Evento e = eventoRepository.findById(evento)
                            .orElse(new br.com.unifef.biblioteca.domains.graph.Evento(evento));
                        eventoRepository.save(e);
                        imagemNode.getEventos().add(e);
                    }
                }

                for (String org : ocrResult.getOrganizacoes()) {
                    if (org != null && !org.trim().isEmpty()) {
                        br.com.unifef.biblioteca.domains.graph.Organizacao o = organizacaoRepository.findById(org)
                            .orElse(new br.com.unifef.biblioteca.domains.graph.Organizacao(org));
                        organizacaoRepository.save(o);
                        imagemNode.getOrganizacoes().add(o);
                    }
                }

                for (String assunto : ocrResult.getAssuntos()) {
                    if (assunto != null && !assunto.trim().isEmpty()) {
                        br.com.unifef.biblioteca.domains.graph.Assunto a = assuntoRepository.findById(assunto)
                            .orElse(new br.com.unifef.biblioteca.domains.graph.Assunto(assunto));
                        assuntoRepository.save(a);
                        imagemNode.getAssuntos().add(a);
                    }
                }

                imagemNodeRepository.save(imagemNode);
            } catch (Exception e) {
                log.error("Erro ao sincronizar OCR da imagem com Neo4J: {}", e.getMessage(), e);
            }
            
            return ocrResult;
        } catch (Exception e) {
            log.error("Erro ao processar OCR para imagem {}: {}", imagemUrl, e.getMessage());
            throw new RuntimeException("Falha no processo de OCR da imagem: " + e.getMessage(), e);
        }
    }

    @Transactional(transactionManager = "transactionManager")
    public int migrarThumbnails() {
        List<Documento> documentos = repository.findAll();
        int count = 0;
        for (Documento doc : documentos) {
            if (doc.getUrlThumbnail() == null && doc.getImagensUrls() != null && !doc.getImagensUrls().isEmpty()) {
                String primeiraImagem = doc.getImagensUrls().get(0);
                try (InputStream is = fileStorageService.fetch(primeiraImagem)) {
                    byte[] imageBytes = is.readAllBytes();
                    String thumbUrl = gerarThumbnail(imageBytes, primeiraImagem);
                    if (thumbUrl != null) {
                        doc.setUrlThumbnail(thumbUrl);
                        repository.save(doc);
                        count++;
                    }
                } catch (Exception e) {
                    log.error("Erro ao migrar thumbnail para documento {}: {}", doc.getId(), e.getMessage());
                }
            }
        }
        return count;
    }

    private String gerarThumbnail(byte[] imageBytes, String originalFilename) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (originalImage == null) return null;

            int targetWidth = 300;
            int targetHeight = (int) (originalImage.getHeight() * (targetWidth / (double) originalImage.getWidth()));

            BufferedImage thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            thumbnail.getGraphics().drawImage(originalImage.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpg", baos);
            byte[] thumbBytes = baos.toByteArray();

            String thumbName = originalFilename.replaceAll("(?i)\\.(jpg|jpeg|png|heic|pdf)$", "") + "_thumb.jpg";
            MultipartFile thumbFile = new MockMultipartFile(thumbName, thumbName, "image/jpeg", new ByteArrayInputStream(thumbBytes));
            
            return fileStorageService.save(thumbFile);
        } catch (Exception e) {
            log.error("Erro ao gerar thumbnail para {}", originalFilename, e);
            return null;
        }
    }
}
