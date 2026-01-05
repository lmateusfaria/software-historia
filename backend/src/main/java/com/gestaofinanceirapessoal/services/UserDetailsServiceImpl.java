package com.gestaofinanceirapessoal.services;

import com.gestaofinanceirapessoal.domains.Usuario;
import com.gestaofinanceirapessoal.repositories.UsuarioRepository;
import com.gestaofinanceirapessoal.security.UserSS;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository repo;

    public UserDetailsServiceImpl(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String rawLogin) throws UsernameNotFoundException {
        if (rawLogin == null) throw new UsernameNotFoundException("Login vazio");

        String login = rawLogin.trim();

        Optional<Usuario> opt;
        if (login.contains("@")) {                             // e-mail
            opt = repo.findByEmailIgnoreCase(login);
        } else if (login.matches("\\d{11}")) {                  // (opcional) CPF
            opt = repo.findByCpf(login);
        } else {                                                // nome/username
            opt = repo.findByNomeIgnoreCase(login);
        }

        Usuario user = opt.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return new UserSS(user);  // seu UserSS já retorna email como username (ótimo p/ token)
    }
}
