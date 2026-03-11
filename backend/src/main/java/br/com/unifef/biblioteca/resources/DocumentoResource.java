package br.com.unifef.biblioteca.resources;

import br.com.unifef.biblioteca.domains.dtos.DocumentoDTO;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import br.com.unifef.biblioteca.services.DocumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import java.util.List;
import java.io.InputStream;

@RestController
@RequestMapping(value = "/documentos")
public class DocumentoResource {

    @Autowired
    private DocumentoService service;

    @GetMapping
    public ResponseEntity<List<DocumentoDTO>> findAll() {
        return ResponseEntity.ok().body(service.findAll());
    }

    @GetMapping(value = "/status/{status}")
    public ResponseEntity<List<DocumentoDTO>> findByStatus(@PathVariable StatusDocumento status) {
        return ResponseEntity.ok().body(service.findByStatus(status));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoDTO> create(
            @RequestPart("documento") DocumentoDTO dto,
            @RequestPart("files") List<MultipartFile> files) {
        return ResponseEntity.ok().body(service.create(dto, files));
    }

    @GetMapping(value = "/download/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        InputStream stream = service.getFileStream(filename);
        InputStreamResource resource = new InputStreamResource(stream);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
