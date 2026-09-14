package br.com.unifef.biblioteca.resources;

import br.com.unifef.biblioteca.config.SecurityConfig;
import br.com.unifef.biblioteca.domains.Usuario;
import br.com.unifef.biblioteca.domains.enums.Perfil;
import br.com.unifef.biblioteca.resources.exceptions.ResourceExceptionHandler;
import br.com.unifef.biblioteca.security.JWTUtils;
import br.com.unifef.biblioteca.security.UserSS;
import br.com.unifef.biblioteca.services.UserDetailsServiceImpl;
import br.com.unifef.biblioteca.services.UsuarioService;
import br.com.unifef.biblioteca.services.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração (MockMvc) para {@link UsuarioResource}: exercitam a cadeia real de
 * segurança (JWT + {@code @PreAuthorize}) e a validação de payload (bean validation), com a
 * camada de serviço mockada.
 */
@WebMvcTest(controllers = UsuarioResource.class)
@Import({UsuarioResource.class, ResourceExceptionHandler.class, SecurityConfig.class, JWTUtils.class})
@TestPropertySource(properties = {
        "jwt.secret=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
        "jwt.expiration=3600000"
})
class UsuarioResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JWTUtils jwtUtils;

    @MockBean
    private UsuarioService usuarioService;

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

    private Usuario usuarioComId(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setCpf("12345678901");
        u.setNome("Usuario Teste");
        u.setEmail("teste@x.com");
        u.setDataCriacao(LocalDate.now());
        u.setPerfil(Perfil.PESQUISADOR);
        u.setPodeCadastrar(false);
        return u;
    }

    // ---------- POST /usuarios (público) ----------

    @Test
    void insert_payloadValido_retorna201ComLocation() throws Exception {
        Usuario salvo = usuarioComId(5L);
        when(usuarioService.create(any())).thenReturn(salvo);

        String body = "{"
                + "\"cpf\":\"52998224725\","
                + "\"nome\":\"Novo Usuario\","
                + "\"email\":\"novo@x.com\","
                + "\"senha\":\"senha123\","
                + "\"perfil\":\"PESQUISADOR\""
                + "}";

        mockMvc.perform(post("/usuarios").contentType("application/json").content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/usuarios/5")));
    }

    @Test
    void insert_emailInvalido_retorna400ComErrosDeValidacao() throws Exception {
        String body = "{"
                + "\"cpf\":\"52998224725\","
                + "\"nome\":\"Novo Usuario\","
                + "\"email\":\"email-invalido\","
                + "\"senha\":\"senha123\","
                + "\"perfil\":\"PESQUISADOR\""
                + "}";

        mockMvc.perform(post("/usuarios").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.fieldName == 'email')]").exists());
    }

    @Test
    void insert_senhaAusente_retorna400() throws Exception {
        String body = "{"
                + "\"cpf\":\"52998224725\","
                + "\"nome\":\"Novo Usuario\","
                + "\"email\":\"novo@x.com\","
                + "\"perfil\":\"PESQUISADOR\""
                + "}";

        mockMvc.perform(post("/usuarios").contentType("application/json").content(body))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET /usuarios (restrito a PROFESSOR) ----------

    @Test
    void findAll_semToken_naoAutorizado() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void findAll_comTokenDePerfilSemPermissao_retorna403() throws Exception {
        String token = tokenPara("aluno@x.com", Perfil.ALUNO);

        mockMvc.perform(get("/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void findAll_comTokenDeProfessor_retorna200ComLista() throws Exception {
        String token = tokenPara("prof@x.com", Perfil.PROFESSOR);
        when(usuarioService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // ---------- GET /usuarios/{id} ----------

    @Test
    void findById_comTokenDeProfessor_retorna200() throws Exception {
        String token = tokenPara("prof@x.com", Perfil.PROFESSOR);
        when(usuarioService.findById(5L)).thenReturn(usuarioComId(5L));

        mockMvc.perform(get("/usuarios/5").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void findById_idInexistente_retorna404() throws Exception {
        String token = tokenPara("prof@x.com", Perfil.PROFESSOR);
        when(usuarioService.findById(99L)).thenThrow(new ObjectNotFoundException("Usuário não encontrado! Id: 99"));

        mockMvc.perform(get("/usuarios/99").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Objeto não encontrado"));
    }

    // ---------- PUT /usuarios/{id} ----------

    @Test
    void update_semPermissao_retorna403() throws Exception {
        String token = tokenPara("pesq@x.com", Perfil.PESQUISADOR);
        String body = "{\"cpf\":\"52998224725\",\"nome\":\"Nome\",\"email\":\"a@a.com\",\"perfil\":\"PESQUISADOR\"}";

        mockMvc.perform(put("/usuarios/5")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_comPermissao_retorna200() throws Exception {
        String token = tokenPara("prof@x.com", Perfil.PROFESSOR);
        when(usuarioService.update(eq(5L), any())).thenReturn(usuarioComId(5L));
        String body = "{\"cpf\":\"52998224725\",\"nome\":\"Nome Atualizado\",\"email\":\"a@a.com\",\"perfil\":\"PESQUISADOR\"}";

        mockMvc.perform(put("/usuarios/5")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());
    }

    // ---------- DELETE /usuarios/{id} ----------

    @Test
    void delete_comPermissao_retorna204() throws Exception {
        String token = tokenPara("prof@x.com", Perfil.PROFESSOR);

        mockMvc.perform(delete("/usuarios/5").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(usuarioService).delete(5L);
    }

    @Test
    void delete_semPermissao_retorna403() throws Exception {
        String token = tokenPara("aluno@x.com", Perfil.ALUNO);

        mockMvc.perform(delete("/usuarios/5").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verify(usuarioService, org.mockito.Mockito.never()).delete(any());
    }

    // ---------- DELETE /usuarios/delete-account ----------

    @Test
    void deleteCurrent_usuarioAutenticado_removeAPropriaConta() throws Exception {
        String token = tokenPara("proprio@x.com", Perfil.PESQUISADOR);
        Usuario proprio = usuarioComId(7L);
        proprio.setEmail("proprio@x.com");
        when(usuarioService.findByEmail("proprio@x.com")).thenReturn(proprio);

        mockMvc.perform(delete("/usuarios/delete-account").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(usuarioService).delete(7L);
    }

    // ---------- GET /usuarios/me ----------

    @Test
    void getMe_usuarioAutenticado_retornaProprioPerfil() throws Exception {
        String token = tokenPara("eu@x.com", Perfil.PESQUISADOR);
        Usuario eu = usuarioComId(8L);
        eu.setEmail("eu@x.com");
        when(usuarioService.findByEmail("eu@x.com")).thenReturn(eu);

        mockMvc.perform(get("/usuarios/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("eu@x.com"));
    }

    @Test
    void getMe_semToken_naoAutorizado() throws Exception {
        mockMvc.perform(get("/usuarios/me"))
                .andExpect(status().is4xxClientError());
    }
}
