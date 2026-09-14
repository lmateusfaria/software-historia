package br.com.unifef.biblioteca.resources;

import br.com.unifef.biblioteca.config.SecurityConfig;
import br.com.unifef.biblioteca.domains.Usuario;
import br.com.unifef.biblioteca.domains.enums.Perfil;
import br.com.unifef.biblioteca.security.JWTUtils;
import br.com.unifef.biblioteca.security.UserSS;
import br.com.unifef.biblioteca.services.UserDetailsServiceImpl;
import br.com.unifef.biblioteca.services.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração (MockMvc) para {@link AuthController}, exercitando a cadeia real de
 * segurança ({@link SecurityConfig}) e o tratamento de exceções de autenticação. A camada de
 * serviço ({@link UsuarioService}, {@link AuthenticationManager}, {@link UserDetailsServiceImpl})
 * é mockada; o {@link JWTUtils} é real para permitir gerar/validar tokens de ponta a ponta.
 */
@WebMvcTest(controllers = AuthController.class)
@Import({AuthController.class, SecurityConfig.class, JWTUtils.class})
@TestPropertySource(properties = {
        "jwt.secret=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
        "jwt.expiration=3600000"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private UsuarioService usuarioService;

    private Usuario usuario(String email, Perfil perfil) {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail(email);
        u.setNome("Usuario Teste");
        u.setSenha("hash");
        u.setPerfil(perfil);
        return u;
    }

    // ---------- /auth/login ----------

    @Test
    void login_credenciaisValidas_retorna200ComToken() throws Exception {
        Authentication autenticacaoOk = new UsernamePasswordAuthenticationToken("prof@x.com", null);
        when(authenticationManager.authenticate(any())).thenReturn(autenticacaoOk);

        String body = "{\"login\":\"prof@x.com\",\"password\":\"senha123\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_credenciaisInvalidas_retorna401ComMensagem() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        String body = "{\"login\":\"prof@x.com\",\"password\":\"senhaErrada\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciais inválidas"));
    }

    // ---------- /auth/validate-password ----------

    @Test
    void validatePassword_loginNoCorpoESenhaCorreta_retorna200True() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("prof@x.com", null));

        String body = "{\"login\":\"prof@x.com\",\"password\":\"senha123\"}";

        mockMvc.perform(post("/auth/validate-password")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void validatePassword_senhaIncorreta_retorna403False() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Senha incorreta"));

        String body = "{\"login\":\"prof@x.com\",\"password\":\"senhaErrada\"}";

        mockMvc.perform(post("/auth/validate-password")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(content().string("false"));
    }

    @Test
    void validatePassword_semLoginNoCorpoESemUsuarioAutenticado_retorna400() throws Exception {
        String body = "{\"password\":\"senha123\"}";

        mockMvc.perform(post("/auth/validate-password")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("false"));
    }

    @Test
    void validatePassword_semLoginNoCorpo_usaUsuarioAutenticadoViaToken() throws Exception {
        Usuario logado = usuario("logado@x.com", Perfil.PESQUISADOR);
        when(userDetailsService.loadUserByUsername("logado@x.com")).thenReturn(new UserSS(logado));
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("logado@x.com", null));

        String token = jwtUtils.generateToken("logado@x.com");
        String body = "{\"password\":\"senha123\"}";

        mockMvc.perform(post("/auth/validate-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // ---------- /auth/forgot-password e /auth/reset-password ----------

    @Test
    void forgotPassword_solicitacaoValida_retorna204EDelegaParaOServico() throws Exception {
        String body = "{\"email\":\"a@a.com\"}";

        mockMvc.perform(post("/auth/forgot-password")
                        .header("Origin", "http://localhost:4200")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNoContent());

        verify(usuarioService).generatePasswordResetToken(eq("a@a.com"), eq("http://localhost:4200"));
    }

    @Test
    void resetPassword_tokenValido_retorna204EDelegaParaOServico() throws Exception {
        String body = "{\"token\":\"abc123\",\"newPassword\":\"novaSenha123\"}";

        mockMvc.perform(post("/auth/reset-password")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNoContent());

        verify(usuarioService).resetPassword("abc123", "novaSenha123");
    }
}
