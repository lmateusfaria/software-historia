package br.com.unifef.biblioteca.services;

import br.com.unifef.biblioteca.domains.Documento;
import br.com.unifef.biblioteca.domains.Usuario;
import br.com.unifef.biblioteca.domains.dtos.DocumentoDTO;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import br.com.unifef.biblioteca.repositories.DocumentoRepository;
import br.com.unifef.biblioteca.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DocumentoRepository repository;

    @Autowired
    private FileStorageService fileStorageService;

    public List<DocumentoDTO> findAll() {
        return repository.findAll().stream().map(DocumentoDTO::new).collect(Collectors.toList());
    }

    public List<DocumentoDTO> findByStatus(StatusDocumento status) {
        return repository.findByStatus(status).stream().map(DocumentoDTO::new).collect(Collectors.toList());
    }

    public DocumentoDTO create(DocumentoDTO dto, List<MultipartFile> files) {
        Documento doc = new Documento();
        doc.setTitulo(dto.getTitulo());
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
            List<String> urls = files.stream()
                    .map(fileStorageService::save)
                    .collect(Collectors.toList());
            doc.setImagensUrls(urls);
        }

        return new DocumentoDTO(repository.save(doc));
    }
}
