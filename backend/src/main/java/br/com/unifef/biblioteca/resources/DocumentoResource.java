package br.com.unifef.biblioteca.resources;

import br.com.unifef.biblioteca.domains.dtos.DocumentoDTO;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import br.com.unifef.biblioteca.services.DocumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/documentos")
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

    @PostMapping
    public ResponseEntity<DocumentoDTO> create(@RequestBody DocumentoDTO dto) {
        return ResponseEntity.ok().body(service.create(dto));
    }
}
