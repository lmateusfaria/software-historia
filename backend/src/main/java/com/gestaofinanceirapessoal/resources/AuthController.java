package com.gestaofinanceirapessoal.resources;

import com.gestaofinanceirapessoal.domains.dtos.CredenciaisDTO;
import com.gestaofinanceirapessoal.domains.dtos.TokenDTO;
import com.gestaofinanceirapessoal.security.JWTUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, JWTUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody CredenciaisDTO credenciais) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credenciais.getLogin(),  // nome OU email
                            credenciais.getPassword()
                    )
            );

            // PEGA O "username" do UserDetails (no seu UserSS é o e-mail)
            String subject = authentication.getName(); // equivale a ((UserDetails) authentication.getPrincipal()).getUsername()

            String token = jwtUtils.generateToken(subject);
            return ResponseEntity.ok(new TokenDTO(token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/validate-password")
    public ResponseEntity<Boolean> validatePassword(@RequestBody CredenciaisDTO credenciais,
                                                    Authentication currentAuth) {
        try {
            String username = credenciais.getLogin();
            if (username == null || username.isBlank()) {
                // Usa o usuário autenticado via JWT quando o login não é enviado no corpo
                username = currentAuth != null ? currentAuth.getName() : null;
            }

            if (username == null || credenciais.getPassword() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, credenciais.getPassword())
            );

            // Se a autenticação for bem-sucedida, retorna true
            return ResponseEntity.ok(true);
        } catch (AuthenticationException e) {
            // Se a autenticação falhar, retorna false
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }
    }
}
