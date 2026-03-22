package br.com.unifef.biblioteca.resources;

import br.com.unifef.biblioteca.domains.Documento;
import br.com.unifef.biblioteca.domains.dtos.DocumentoDTO;
import br.com.unifef.biblioteca.domains.dtos.OcrResultadoDTO;
import br.com.unifef.biblioteca.domains.dtos.ImagemBuscaDTO;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import br.com.unifef.biblioteca.services.DocumentoService;
import br.com.unifef.biblioteca.services.GptOcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import java.util.List;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping(value = "/documentos")
public class DocumentoResource {

    @Autowired
    private DocumentoService service;

    @Autowired
    private GptOcrService gptOcrService;

    @GetMapping
    public ResponseEntity<List<DocumentoDTO>> findAll() {
        return ResponseEntity.ok().body(service.findAll());
    }

    @GetMapping(value = "/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String termo,
            @RequestParam(defaultValue = "documentos") String modo) {
        if ("imagens".equalsIgnoreCase(modo)) {
            return ResponseEntity.ok().body(service.searchEnrichedImages(termo));
        }
        return ResponseEntity.ok().body(service.searchEnrichedDocuments(termo));
    }

    @GetMapping(value = "/status/{status}")
    public ResponseEntity<List<DocumentoDTO>> findByStatus(@PathVariable StatusDocumento status) {
        return ResponseEntity.ok().body(service.findByStatus(status));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<DocumentoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok().body(service.findById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoDTO> create(
            @RequestPart("documento") DocumentoDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.ok().body(service.create(dto, files));
    }

    @PostMapping(value = "/upload-chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadChunk(
            @RequestPart("chunk") MultipartFile chunk,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("filename") String filename) {
        
        String mergedFilePath = service.handleChunk(chunk, uploadId, chunkIndex, totalChunks, filename);
        
        Map<String, String> response = new HashMap<>();
        if (mergedFilePath != null) {
            response.put("filePath", mergedFilePath);
            response.put("status", "COMPLETED");
        } else {
            response.put("status", "PROCESSING");
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/download/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        InputStream stream = service.getFileStream(filename);
        InputStreamResource resource = new InputStreamResource(stream);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .body(resource);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}/aprovar")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<DocumentoDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok().body(service.approve(id));
    }

    @PostMapping(value = "/testar-ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<OcrResultadoDTO> testarOcr(
            @RequestPart("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "image/jpeg";
            }
            OcrResultadoDTO resultado = gptOcrService.extrairDadosImagem(file.getBytes(), contentType);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            System.err.println("Erro no teste OCR: " + e.getMessage());
            e.printStackTrace();
            OcrResultadoDTO erro = new OcrResultadoDTO();
            erro.setTextoCompleto("Erro ao processar: " + e.getMessage());
            return ResponseEntity.internalServerError().body(erro);
        }
    }

    @PostMapping(value = "/{id}/ocr-imagem")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<OcrResultadoDTO> ocrImagem(@PathVariable Long id, @RequestParam String imagemUrl) {
        return ResponseEntity.ok(service.processarOcrImagem(id, imagemUrl));
    }

    @PostMapping(value = "/migrar-thumbnails")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Map<String, String>> migrarThumbnails() {
        int processados = service.migrarThumbnails();
        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Processamento de thumbnails finalizado");
        response.put("processados", String.valueOf(processados));
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/migrar-previews")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Map<String, String>> migrarPreviews() {
        int processados = service.migrarPreviews();
        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Processamento de previews finalizado");
        response.put("processados", String.valueOf(processados));
        return ResponseEntity.ok(response);
    }
}
