package br.com.unifef.biblioteca.resources;

import br.com.unifef.biblioteca.config.SecurityConfig;
import br.com.unifef.biblioteca.domains.Usuario;
import br.com.unifef.biblioteca.domains.dtos.OperationalSummaryDTO;
import br.com.unifef.biblioteca.domains.dtos.SystemHealthDTO;
import br.com.unifef.biblioteca.domains.enums.Perfil;
import br.com.unifef.biblioteca.security.JWTUtils;
import br.com.unifef.biblioteca.security.UserSS;
import br.com.unifef.biblioteca.services.SystemHealthService;
import br.com.unifef.biblioteca.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração (MockMvc) para {@link SystemHealthResource}.
 * <p>
 * {@code /system/health} está na lista de URLs públicas de {@link SecurityConfig}, mas
 * {@code /system/summary} NÃO está — cai na regra padrão {@code anyRequest().authenticated()}
 * e, por não ter {@code @PreAuthorize} no controller, fica acessível a QUALQUER perfil
 * autenticado (inclusive PESQUISADOR), expondo contagens operacionais do sistema. Os testes
 * abaixo documentam esse comportamento tal como configurado hoje.
 */
@WebMvcTest(controllers = SystemHealthResource.class)
@Import({SystemHealthResource.class, SecurityConfig.class, JWTUtils.class})
@TestPropertySource(properties = {
        "jwt.secret=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
        "jwt.expiration=3600000"
})
class SystemHealthResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JWTUtils jwtUtils;

    @MockBean
    private SystemHealthService systemHealthService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private String tokenPara(String email, Perfil perfil) {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail(email);
        u.setNome("Usuario Teste");
        u.setSenha("hash");
        u.setPerfil(perfil);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(new UserSS(u));
        return jwtUtils.generateToken(email);
    }

    // ---------- GET /system/health (público) ----------

    @Test
    void health_semAutenticacao_retorna200() throws Exception {
        when(systemHealthService.getHealth())
                .thenReturn(new SystemHealthDTO("UP", "2026-09-02T10:00:00", Collections.emptyList()));

        mockMvc.perform(get("/system/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    // ---------- GET /system/summary (não está na lista pública) ----------

    @Test
    void summary_semAutenticacao_naoAutorizado() throws Exception {
        mockMvc.perform(get("/system/summary"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void summary_comQualquerUsuarioAutenticado_retorna200() throws Exception {
        String token = tokenPara("pesq@x.com", Perfil.PESQUISADOR);
        when(systemHealthService.getOperationalSummary()).thenReturn(new OperationalSummaryDTO());

        mockMvc.perform(get("/system/summary").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
