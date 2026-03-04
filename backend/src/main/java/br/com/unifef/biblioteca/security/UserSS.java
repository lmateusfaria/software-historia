package br.com.unifef.biblioteca.security;

import br.com.unifef.biblioteca.domains.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserSS implements UserDetails {

    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public UserSS(Usuario user) {
        this.username = user.getEmail();
        this.password = user.getSenha();

        // Mapeia o perfil para a autoridade do Spring Security
        String role = user.getPerfil() != null ? user.getPerfil().getDescricao() : "ROLE_PESQUISADOR";
        this.authorities = List.of(new SimpleGrantedAuthority(role));
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
