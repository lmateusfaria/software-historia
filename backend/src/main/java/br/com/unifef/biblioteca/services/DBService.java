package br.com.unifef.biblioteca.services;

import br.com.unifef.biblioteca.domains.*;
import br.com.unifef.biblioteca.domains.enums.Perfil;
import br.com.unifef.biblioteca.repositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DBService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder encoder;

    public void initDB() {
        try {
            if (!usuarioRepository.existsByCpf("34637449618") && !usuarioRepository.existsByEmail("adm@unifef.edu.br")) {
                Usuario user1 = new Usuario(null, "34637449618", "Administrador UNIFEF",
                        "adm@unifef.edu.br", encoder.encode("1234"), LocalDate.now(), Perfil.PROFESSOR);
                usuarioRepository.save(user1);
            }

            if (!usuarioRepository.existsByCpf("16963985332") && !usuarioRepository.existsByEmail("aluno@unifef.edu.br")) {
                Usuario user2 = new Usuario(null, "16963985332", "Aluno Pesquisador UNIFEF",
                        "aluno@unifef.edu.br", encoder.encode("1234"), LocalDate.now(), Perfil.ALUNO);
                usuarioRepository.save(user2);
            }

        } catch (Exception e) {
            System.err.println("Aviso: Tabelas ainda não criadas. O Hibernate deve gerá-las no próximo boot ou durante esse ciclo. Erro: " + e.getMessage());
        }
    }
}
