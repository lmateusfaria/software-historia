package br.com.unifef.biblioteca.repositories;

import br.com.unifef.biblioteca.domains.Documento;
import br.com.unifef.biblioteca.domains.Usuario;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    List<Documento> findByResponsavel(Usuario responsavel);
    List<Documento> findByStatus(StatusDocumento status);
}
