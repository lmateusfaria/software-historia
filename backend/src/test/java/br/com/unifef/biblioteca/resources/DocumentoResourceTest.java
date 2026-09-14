package br.com.unifef.biblioteca.resources;

import br.com.unifef.biblioteca.config.SecurityConfig;
import br.com.unifef.biblioteca.domains.Usuario;
import br.com.unifef.biblioteca.domains.dtos.DocumentoDTO;
import br.com.unifef.biblioteca.domains.dtos.OcrResultadoDTO;
import br.com.unifef.biblioteca.domains.dtos.OcrResultadoUpdateDTO;
import br.com.unifef.biblioteca.domains.dtos.OcrStatusDTO;
import br.com.unifef.biblioteca.domains.enums.Perfil;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import br.com.unifef.biblioteca.resources.exceptions.ResourceExceptionHandler;
import br.com.unifef.biblioteca.security.JWTUtils;
import br.com.unifef.biblioteca.security.UserSS;
import br.com.unifef.biblioteca.services.DocumentoService;
import br.com.unifef.biblioteca.services.GptOcrService;
import br.com.unifef.biblioteca.services.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração (MockMvc) para {@link DocumentoResource}: exercitam as regras de
 * visibilidade pública x restrita configuradas em {@link SecurityConfig} (GET público de
 * listagem/detalhe, download público, e as demais operações restritas por
 * {@code @PreAuthorize}), com a camada de serviço mockada.
 */
@WebMvcTest(controllers = DocumentoResource.class)
@Import({DocumentoResource.class, ResourceExceptionHandler.class, SecurityConfig.class, JWTUtils.class})
@TestPropertySource(properties = {
        "jwt.secret=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
        "jwt.expiration=3600000"
})
class DocumentoResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentoService service;

    @MockBean
    private GptOcrService gptOcrService;

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

    private DocumentoDTO documento(Long id, StatusDocumento status) {
        DocumentoDTO dto = new DocumentoDTO();
        dto.setId(id);
        dto.setDescricao("Documento " + id);
        dto.setStatus(status);
        return dto;
    }

    // ---------- GET /documentos (público) ----------

    @Test
    void findAll_semAutenticacao_retorna200() throws Exception {
        when(service.findAll()).thenReturn(List.of(documento(1L, StatusDocumento.APROVADO)));

        mockMvc.perform(get("/documentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void findById_semAutenticacao_retorna200() throws Exception {
        when(service.findById(1L)).thenReturn(documento(1L, StatusDocumento.APROVADO));

        mockMvc.perform(get("/documentos/1"))
                .andExpect(status().isOk());
    }

    // ---------- GET /documentos/download/{filename} (público) ----------

    @Test
    void download_arquivoExistente_retorna200ComCacheHeaders() throws Exception {
        InputStream stream = new ByteArrayInputStream(new byte[] {1, 2, 3});
        when(service.getFileStream("img1.jpg")).thenReturn(stream);

        mockMvc.perform(get("/documentos/download/img1.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "public, max-age=31536000, immutable"));
    }

    @Test
    void download_arquivoInexistente_retorna404() throws Exception {
        when(service.getFileStream("nao-existe.jpg")).thenReturn(null);

        mockMvc.perform(get("/documentos/download/nao-existe.jpg"))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE /documentos/{id} (restrito a PROFESSOR) ----------

    @Test
    void delete_semToken_naoAutorizado() throws Exception {
        mockMvc.perform(delete("/documentos/1"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void delete_comTokenDeAluno_retorna403() throws Exception {
        String token = tokenPara("aluno@x.com", Perfil.ALUNO);

        mockMvc.perform(delete("/documentos/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verify(service, org.mockito.Mockito.never()).delete(anyLong());
    }

    @Test
    void delete_comTokenDeProfessor_retorna204() throws Exception {
        String token = tokenPara("prof@x.com", Perfil.PROFESSOR);

        mockMvc.perform(delete("/documentos/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    // ---------- PUT /documentos/{id}/aprovar (restrito a PROFESSOR) ----------

    @Test
    void approve_comTokenDeProfessor_retorna200() throws Exception {
        String token = tokenPara("prof@x.com", Perfil.PROFESSOR);
        when(service.approve(1L)).thenReturn(documento(1L, StatusDocumento.APROVADO));

        mockMvc.perform(put("/documentos/1/aprovar").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));
    }

    @Test
    void approve_semPermissao_retorna403() throws Exception {
        String token = tokenPara("pesq@x.com", Perfil.PESQUISADOR);

        mockMvc.perform(put("/documentos/1/aprovar").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ---------- PUT /documentos/{id}/ocr-resultados/{ocrResultadoId} (PROFESSOR ou ALUNO) ----------

    @Test
    void atualizarOcrResultado_comTokenDeAluno_retorna200() throws Exception {
        String token = tokenPara("aluno@x.com", Perfil.ALUNO);
        OcrResultadoDTO resultado = new OcrResultadoDTO();
        resultado.setId(10L);
        resultado.setTextoCompleto("Texto revisado");
        when(service.atualizarOcrResultado(eq(1L), eq(10L), any(OcrResultadoUpdateDTO.class))).thenReturn(resultado);

        OcrResultadoUpdateDTO dto = new OcrResultadoUpdateDTO();
        dto.setTextoCompleto("Texto revisado");

        mockMvc.perform(put("/documentos/1/ocr-resultados/10")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.textoCompleto").value("Texto revisado"));
    }

    @Test
    void atualizarOcrResultado_comTokenDePesquisador_retorna403() throws Exception {
        String token = tokenPara("pesq@x.com", Perfil.PESQUISADOR);
        OcrResultadoUpdateDTO dto = new OcrResultadoUpdateDTO();
        dto.setTextoCompleto("Texto revisado");

        mockMvc.perform(put("/documentos/1/ocr-resultados/10")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ---------- DELETE /documentos/{id}/ocr-resultados/{ocrResultadoId} ----------

    @Test
    void excluirOcrResultado_comTokenDeProfessor_retorna204() throws Exception {
        String token = tokenPara("prof@x.com", Perfil.PROFESSOR);

        mockMvc.perform(delete("/documentos/1/ocr-resultados/10").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(service).excluirOcrResultado(1L, 10L);
    }

    // ---------- POST /documentos/{id}/ocr-todas-paginas (restrito a PROFESSOR) ----------

    @Test
    void ocrTodasPaginas_comTokenDeProfessor_retorna200() throws Exception {
        String token = tokenPara("prof@x.com", Perfil.PROFESSOR);
        when(service.processarOcrTodasPaginas(1L))
                .thenReturn(new OcrStatusDTO("PROCESSANDO", "Extração iniciada para 2 página(s).", 1L, null));

        mockMvc.perform(post("/documentos/1/ocr-todas-paginas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSANDO"));
    }

    @Test
    void ocrTodasPaginas_semToken_naoAutorizado() throws Exception {
        mockMvc.perform(post("/documentos/1/ocr-todas-paginas"))
                .andExpect(status().is4xxClientError());
    }

    // ---------- POST /documentos/testar-ocr (restrito a PROFESSOR) ----------

    @Test
    void testarOcr_comTokenDeProfessor_retorna200() throws Exception {
        String token = tokenPara("prof@x.com", Perfil.PROFESSOR);
        OcrResultadoDTO resultado = new OcrResultadoDTO();
        resultado.setTextoCompleto("Texto extraido de teste");
        when(gptOcrService.extrairDadosImagem(any(byte[].class), any(String.class))).thenReturn(resultado);

        MockMultipartFile file = new MockMultipartFile("file", "pagina.jpg", "image/jpeg", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/documentos/testar-ocr")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.textoCompleto").value("Texto extraido de teste"));
    }

    @Test
    void testarOcr_semPermissao_retorna403() throws Exception {
        String token = tokenPara("aluno@x.com", Perfil.ALUNO);
        MockMultipartFile file = new MockMultipartFile("file", "pagina.jpg", "image/jpeg", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/documentos/testar-ocr")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
