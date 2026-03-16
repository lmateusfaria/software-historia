package br.com.unifef.biblioteca.repositories;

import br.com.unifef.biblioteca.domains.ImagemOcrResultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagemOcrRepository extends JpaRepository<ImagemOcrResultado, Long> {
    List<ImagemOcrResultado> findByDocumentoIdOrderByIndice(Long documentoId);
}
