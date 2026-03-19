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
import br.com.unifef.biblioteca.events.DocumentoCriadoEvent;
import org.springframework.context.ApplicationEventPublisher;
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
import java.awt.Graphics2D;
import java.awt.RenderingHints;

@Slf4j
@Service
public class DocumentoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

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
        doc.setStatus(StatusDocumento.PROCESSANDO); // Status inicial durante o processamento assíncrono
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

        Documento savedDoc = repository.save(doc);

        List<String> allFilePaths = new ArrayList<>();
        
        // Se houver arquivos pré-upados (chunks), adicionamos à lista
        if (dto.getPreUploadedFiles() != null) {
            allFilePaths.addAll(dto.getPreUploadedFiles());
        }

        // Se houver MultipartFiles novos, salvamos temporariamente para processamento assíncrono
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    String tempName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), tempName);
                    file.transferTo(tempPath.toFile());
                    allFilePaths.add(tempPath.toString());
                } catch (Exception e) {
                    log.error("Erro ao salvar arquivo temporário para processamento assíncrono", e);
                }
            }
        }

        // Sincronização básica com Neo4j (Grafo)
        try {
            DocumentoNode node = new DocumentoNode(savedDoc.getId(), savedDoc.getEdicao() != null ? savedDoc.getEdicao() : savedDoc.getTipo());
            documentoNodeRepository.save(node);
        } catch (Exception e) {
            log.warn("Erro ao criar node inicial no Neo4j: {}", e.getMessage());
        }

        // Disparar o processamento assíncrono via Evento
        eventPublisher.publishEvent(new DocumentoCriadoEvent(this, savedDoc.getId(), allFilePaths));

        return new DocumentoDTO(savedDoc);
    }

    /**
     * Método chamado assincronamente pelo DocumentoEventListener
     */
    @Transactional
    public void processarDocumentoAssincrono(Long documentoId, List<String> filePaths) {
        log.info("Processando documento {} com {} arquivos", documentoId, filePaths != null ? filePaths.size() : 0);
        Documento doc = repository.findById(documentoId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado para processamento assíncrono"));

        if (filePaths == null || filePaths.isEmpty()) {
            doc.setStatus(StatusDocumento.PENDENTE_OCR);
            repository.save(doc);
            return;
        }

        List<String> finalUrls = new ArrayList<>();
        List<File> tempFilesToDelete = new ArrayList<>();

        try {
            List<CompletableFuture<List<String>>> futures = new ArrayList<>();

            for (String path : filePaths) {
                File file = new File(path);
                if (!file.exists()) continue;

                tempFilesToDelete.add(file);
                String filename = file.getName();
                String contentType = Files.probeContentType(file.toPath());

                if (contentType != null && contentType.equalsIgnoreCase("application/pdf")) {
                    futures.add(processPdfFromDisk(file));
                } else if (filename.toLowerCase().endsWith(".heic")) {
                    futures.add(CompletableFuture.supplyAsync(() -> 
                        Collections.singletonList(processHeicFromDisk(file)), generalExecutor));
                } else {
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        try (InputStream is = new java.io.FileInputStream(file)) {
                            MultipartFile multipartFile = new MockMultipartFile(
                                filename, filename, contentType, is
                            );
                            return Collections.singletonList(fileStorageService.save(multipartFile));
                        } catch (Exception e) {
                            log.error("Erro ao salvar imagem do disco: {}", filename, e);
                            return Collections.emptyList();
                        }
                    }, generalExecutor));
                }
            }

            // Aguardar todos os processamentos
            for (CompletableFuture<List<String>> future : futures) {
                finalUrls.addAll(future.join());
            }

            doc.setImagensUrls(finalUrls);
            repository.saveAndFlush(doc); // Garante que as imagens originais persistiram

            // Gerar thumbnails e previews para todas as imagens de forma paralela
            List<CompletableFuture<Void>> processingFutures = new ArrayList<>();
            List<String> thumbnails = Collections.synchronizedList(new ArrayList<>());
            List<String> previews = Collections.synchronizedList(new ArrayList<>());

            for (String imageUrl : finalUrls) {
                processingFutures.add(CompletableFuture.runAsync(() -> {
                    try (InputStream is = fileStorageService.fetch(imageUrl)) {
                        byte[] imageBytes = is.readAllBytes();
                        
                        // Gerar Thumbnail (300px)
                        String thumbUrl = gerarThumbnail(imageBytes, imageUrl);
                        if (thumbUrl != null) thumbnails.add(thumbUrl);
                        
                        // Gerar Preview (1200px) - NOVO
                        String previewUrl = gerarPreview(imageBytes, imageUrl);
                        if (previewUrl != null) previews.add(previewUrl);
                        
                    } catch (Exception e) {
                        log.warn("Falha ao gerar processamento visual para {}: {}", imageUrl, e.getMessage());
                    }
                }, generalExecutor));
            }

            // Aguardar todos os processamentos visuais
            CompletableFuture.allOf(processingFutures.toArray(new CompletableFuture[0])).join();

            // Ordenar para manter correspondência com as imagens originais (opcional, mas bom)
            // Aqui simplificamos salvando as listas conforme geradas
            doc.setThumbnailsUrls(new ArrayList<>(thumbnails));
            doc.setPreviewsUrls(new ArrayList<>(previews));
            
            if (!thumbnails.isEmpty()) doc.setUrlThumbnail(thumbnails.get(0));
            if (!previews.isEmpty()) doc.setUrlPreview(previews.get(0));

            // Finalizar processamento
            doc.setStatus(StatusDocumento.PENDENTE_OCR);
            repository.save(doc);
            log.info("Processamento assíncrono do documento {} finalizado. Imagens: {}, Thumbs: {}, Previews: {}", 
                documentoId, finalUrls.size(), thumbnails.size(), previews.size());

        } catch (Exception e) {
            log.error("Erro grave no processamento assíncrono do documento {}: {}", documentoId, e.getMessage(), e);
            doc.setStatus(StatusDocumento.ERRO);
            repository.save(doc);
        } finally {
            // Limpeza de arquivos temporários
            for (File f : tempFilesToDelete) {
                try {
                    Files.deleteIfExists(f.toPath());
                    // Tenta remover pastas de chunks se ficarem vazias
                    if (f.getParentFile().getName().startsWith("upload-")) {
                         Files.deleteIfExists(f.getParentFile().toPath());
                    }
                } catch (Exception ex) {
                    log.warn("Erro ao deletar arquivo temporário: {}", f.getAbsolutePath());
                }
            }
        }
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
            Files.copy(chunk.getInputStream(), chunkPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

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
        
        // 1. Remover arquivos do MinIO (Imagens e Thumbnails)
        List<String> allFiles = new ArrayList<>();
        if (doc.getImagensUrls() != null) allFiles.addAll(doc.getImagensUrls());
        if (doc.getThumbnailsUrls() != null) allFiles.addAll(doc.getThumbnailsUrls());
        if (doc.getPreviewsUrls() != null) allFiles.addAll(doc.getPreviewsUrls());

        for (String filename : allFiles) {
            try {
                fileStorageService.delete(filename);
            } catch (Exception e) {
                log.error("Erro ao deletar arquivo no MinIO: {}. Erro: {}", filename, e.getMessage());
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
            
            // 2. Pré-processamento avançado da imagem (Grayscale, Crop, Resize, JPEG)
            byte[] processedBytes = preProcessarImagemParaOcr(imageBytes);
            
            // 3. Processar OCR GPT-4o-mini com imagem otimizada
            OcrResultadoDTO ocrResult = gptOcrService.extrairDadosImagem(processedBytes, "image/jpeg");

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
            boolean mudou = false;
            List<String> thumbs = doc.getThumbnailsUrls() != null ? doc.getThumbnailsUrls() : new ArrayList<>();
            
            if (doc.getImagensUrls() != null && !doc.getImagensUrls().isEmpty()) {
                // Se não tem a lista completa de thumbs, tenta gerar para todas
                if (thumbs.size() < doc.getImagensUrls().size()) {
                    thumbs.clear();
                    for (String imgUrl : doc.getImagensUrls()) {
                        try (InputStream is = fileStorageService.fetch(imgUrl)) {
                            byte[] imageBytes = is.readAllBytes();
                            String thumbUrl = gerarThumbnail(imageBytes, imgUrl);
                            if (thumbUrl != null) thumbs.add(thumbUrl);
                        } catch (Exception e) {
                            log.error("Erro ao migrar thumb p/ {}: {}", imgUrl, e.getMessage());
                        }
                    }
                    doc.setThumbnailsUrls(thumbs);
                    if (!thumbs.isEmpty()) doc.setUrlThumbnail(thumbs.get(0));
                    mudou = true;
                }
            }
            
            if (mudou) {
                repository.save(doc);
                count++;
            }
        }
        return count;
    }

    @Transactional(transactionManager = "transactionManager")
    public int migrarPreviews() {
        List<Documento> documentos = repository.findAll();
        int count = 0;
        for (Documento doc : documentos) {
            boolean mudou = false;
            List<String> previews = doc.getPreviewsUrls() != null ? doc.getPreviewsUrls() : new ArrayList<>();
            
            if (doc.getImagensUrls() != null && !doc.getImagensUrls().isEmpty()) {
                if (previews.size() < doc.getImagensUrls().size()) {
                    previews.clear();
                    for (String imgUrl : doc.getImagensUrls()) {
                        try (InputStream is = fileStorageService.fetch(imgUrl)) {
                            byte[] imageBytes = is.readAllBytes();
                            String prevUrl = gerarPreview(imageBytes, imgUrl);
                            if (prevUrl != null) previews.add(prevUrl);
                        } catch (Exception e) {
                            log.error("Erro ao migrar preview p/ {}: {}", imgUrl, e.getMessage());
                        }
                    }
                    doc.setPreviewsUrls(previews);
                    if (!previews.isEmpty()) doc.setUrlPreview(previews.get(0));
                    mudou = true;
                }
            }
            
            if (mudou) {
                repository.save(doc);
                count++;
            }
        }
        return count;
    }

    private String gerarThumbnail(byte[] imageBytes, String originalFilename) {
        return gerarImagemRedimensionada(imageBytes, originalFilename, 300, "_thumb");
    }

    private String gerarPreview(byte[] imageBytes, String originalFilename) {
        return gerarImagemRedimensionada(imageBytes, originalFilename, 1200, "_preview");
    }

    private String gerarImagemRedimensionada(byte[] imageBytes, String originalFilename, int targetWidth, String suffix) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (originalImage == null) return null;

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            
            // Se a imagem original for menor que o alvo, não redimensiona (ou redimensiona para o original)
            int finalWidth = Math.min(targetWidth, width);
            int finalHeight = (int) (height * (finalWidth / (double) width));

            BufferedImage resized = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage, 0, 0, finalWidth, finalHeight, null);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "jpg", baos);
            byte[] outputBytes = baos.toByteArray();

            String newName = originalFilename.replaceAll("(?i)\\.(jpg|jpeg|png|heic|pdf)$", "") + suffix + ".jpg";
            MultipartFile multipartFile = new MockMultipartFile(newName, newName, "image/jpeg", new ByteArrayInputStream(outputBytes));
            
            return fileStorageService.save(multipartFile);
        } catch (Exception e) {
            log.error("Erro ao gerar {} para {}", suffix, originalFilename, e);
            return null;
        }
    }

    private byte[] preProcessarImagemParaOcr(byte[] imageBytes) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (originalImage == null) return imageBytes;

            // 1. Redimensionar se for muito grande (max 2000px)
            int maxDimension = 2000;
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            
            if (width > maxDimension || height > maxDimension) {
                double scale = Math.min((double) maxDimension / width, (double) maxDimension / height);
                int newWidth = (int) (width * scale);
                int newHeight = (int) (height * scale);
                
                BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g = resized.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                g.dispose();
                originalImage = resized;
            } else {
                // Converter para Escala de Cinza se não redimensionou
                BufferedImage gray = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g = gray.createGraphics();
                g.drawImage(originalImage, 0, 0, null);
                g.dispose();
                originalImage = gray;
            }

            // 2. Crop Automático (Remover bordas brancas/vazias)
            originalImage = autoCrop(originalImage);

            // 3. Exportar como JPEG comprimido
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(originalImage, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erro no pré-processamento da imagem: {}", e.getMessage());
            return imageBytes;
        }
    }

    private BufferedImage autoCrop(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int top = 0, left = 0, bottom = height - 1, right = width - 1;

        // Limiar para considerar "branco" (em escala de cinza 0-255, 240+ é quase branco)
        int threshold = 240;

        // Encontrar o topo
        outer: for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((image.getRGB(x, y) & 0xFF) < threshold) {
                    top = y;
                    break outer;
                }
            }
        }
        // Encontrar a base
        outer: for (int y = height - 1; y >= top; y--) {
            for (int x = 0; x < width; x++) {
                if ((image.getRGB(x, y) & 0xFF) < threshold) {
                    bottom = y;
                    break outer;
                }
            }
        }
        // Encontrar a esquerda
        outer: for (int x = 0; x < width; x++) {
            for (int y = top; y <= bottom; y++) {
                if ((image.getRGB(x, y) & 0xFF) < threshold) {
                    left = x;
                    break outer;
                }
            }
        }
        // Encontrar a direita
        outer: for (int x = width - 1; x >= left; x--) {
            for (int y = top; y <= bottom; y++) {
                if ((image.getRGB(x, y) & 0xFF) < threshold) {
                    right = x;
                    break outer;
                }
            }
        }

        int newWidth = right - left + 1;
        int newHeight = bottom - top + 1;
        
        // Validar limites para evitar exceção de crop inválido
        if (newWidth <= 0 || newHeight <= 0 || (left == 0 && top == 0 && right == width - 1 && bottom == height - 1)) {
            return image;
        }

        return image.getSubimage(left, top, newWidth, newHeight);
    }
}
