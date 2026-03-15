package br.com.unifef.biblioteca.repositories;

import br.com.unifef.biblioteca.domains.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);
    Optional<Usuario> findByNomeIgnoreCase(String nome);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    Optional<Usuario> findByNome(String nome);

    Optional<Usuario> findByResetToken(String resetToken);
}
