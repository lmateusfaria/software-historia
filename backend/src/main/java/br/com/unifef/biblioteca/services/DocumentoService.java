package br.com.unifef.biblioteca.services;

import br.com.unifef.biblioteca.domains.Documento;
import br.com.unifef.biblioteca.domains.Usuario;
import br.com.unifef.biblioteca.domains.dtos.DocumentoDTO;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import br.com.unifef.biblioteca.repositories.DocumentoRepository;
import br.com.unifef.biblioteca.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentoService {

    @Autowired
    private DocumentoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<DocumentoDTO> findAll() {
        return repository.findAll().stream().map(DocumentoDTO::new).collect(Collectors.toList());
    }

    public List<DocumentoDTO> findByStatus(StatusDocumento status) {
        return repository.findByStatus(status).stream().map(DocumentoDTO::new).collect(Collectors.toList());
    }

    public DocumentoDTO create(DocumentoDTO dto) {
        Documento doc = new Documento();
        doc.setTitulo(dto.getTitulo());
        doc.setDescricao(dto.getDescricao());
        // doc.setUrlImagem(dto.getUrlImagem()); // Entidade usa imagensUrls (List)
        doc.setStatus(br.com.unifef.biblioteca.domains.StatusDocumento.AGUARDANDO_APROVACAO);
        doc.setDataDigitalizacao(LocalDate.now());
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        doc.setResponsavel(usuario);

        return new DocumentoDTO(repository.save(doc));
    }
}
