package br.com.unifef.biblioteca.services;

import br.com.unifef.biblioteca.domains.Usuario;
import br.com.unifef.biblioteca.domains.dtos.UsuarioDTO;
import br.com.unifef.biblioteca.domains.dtos.UsuarioUpdateDTO;
import br.com.unifef.biblioteca.domains.enums.Perfil;
import br.com.unifef.biblioteca.repositories.UsuarioRepository;
import br.com.unifef.biblioteca.security.UserSS;
import br.com.unifef.biblioteca.services.exceptions.AuthorizationException;
import br.com.unifef.biblioteca.services.exceptions.DataIntegrityViolationException;
import br.com.unifef.biblioteca.services.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para as regras de negócio de {@link UsuarioService}:
 * cadastro com controle de permissão por perfil, unicidade de CPF/e-mail
 * e fluxo de recuperação de senha.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

    private Usuario criarUsuario(Long id, String cpf, String email, Perfil perfil, boolean podeCadastrar) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setCpf(cpf);
        u.setEmail(email);
        u.setNome("Usuario Teste");
        u.setPerfil(perfil);
        u.setPodeCadastrar(podeCadastrar);
        return u;
    }

    private UsuarioDTO criarDto(String cpf, String email, Perfil perfil) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setCpf(cpf);
        dto.setEmail(email);
        dto.setNome("Novo Usuario");
        dto.setSenha("senha123");
        dto.setPerfil(perfil);
        return dto;
    }

    // ---------- findById / findByCpf / findByEmail ----------

    @Test
    void findById_quandoExiste_retornaUsuario() {
        Usuario u = criarUsuario(1L, "111", "a@a.com", Perfil.PESQUISADOR, false);
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(u));

        Usuario resultado = usuarioService.findById(1L);

        assertThat(resultado).isEqualTo(u);
    }

    @Test
    void findById_quandoNaoExiste_lancaObjectNotFoundException() {
        when(usuarioRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findById(99L))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void findByCpf_quandoNaoExiste_lancaObjectNotFoundException() {
        when(usuarioRepo.findByCpf("000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findByCpf("000"))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void findByEmail_quandoNaoExiste_lancaObjectNotFoundException() {
        when(usuarioRepo.findByEmail("x@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findByEmail("x@x.com"))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    // ---------- create: regras de autorização por perfil ----------

    @Test
    void create_publicoCadastrandoPesquisador_deveTerSucesso() {
        UsuarioDTO dto = criarDto("111", "novo@x.com", Perfil.PESQUISADOR);
        when(encoder.encode("senha123")).thenReturn("hash");
        when(usuarioRepo.findByCpf("111")).thenReturn(Optional.empty());
        when(usuarioRepo.findByEmail("novo@x.com")).thenReturn(Optional.empty());
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario salvo = usuarioService.create(dto);

        assertThat(salvo.getPerfil()).isEqualTo(Perfil.PESQUISADOR);
        assertThat(salvo.getPodeCadastrar()).isFalse();
        assertThat(salvo.getSenha()).isEqualTo("hash");
    }

    @Test
    void create_publicoCadastrandoProfessor_semAutorizacao_lancaAuthorizationException() {
        UsuarioDTO dto = criarDto("111", "novo@x.com", Perfil.PROFESSOR);
        when(encoder.encode(anyString())).thenReturn("hash");

        assertThatThrownBy(() -> usuarioService.create(dto))
                .isInstanceOf(AuthorizationException.class);

        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void create_usuarioAutorizadoCadastrandoProfessor_deveTerSucesso() {
        Usuario logado = criarUsuario(5L, "555", "logado@x.com", Perfil.PROFESSOR, true);
        UserSS userSS = new UserSS(logado);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userSS, null, userSS.getAuthorities()));

        UsuarioDTO dto = criarDto("222", "aluno@x.com", Perfil.ALUNO);
        when(encoder.encode(anyString())).thenReturn("hash");
        when(usuarioRepo.findByEmail("logado@x.com")).thenReturn(Optional.of(logado));
        when(usuarioRepo.findByCpf("222")).thenReturn(Optional.empty());
        when(usuarioRepo.findByEmail("aluno@x.com")).thenReturn(Optional.empty());
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario salvo = usuarioService.create(dto);

        assertThat(salvo.getPerfil()).isEqualTo(Perfil.ALUNO);
    }

    @Test
    void create_cpfJaCadastrado_lancaDataIntegrityViolationException() {
        UsuarioDTO dto = criarDto("111", "novo@x.com", Perfil.PESQUISADOR);
        when(encoder.encode(anyString())).thenReturn("hash");
        Usuario existente = criarUsuario(2L, "111", "outro@x.com", Perfil.PESQUISADOR, false);
        when(usuarioRepo.findByCpf("111")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> usuarioService.create(dto))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("CPF");

        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void create_emailJaCadastrado_lancaDataIntegrityViolationException() {
        UsuarioDTO dto = criarDto("111", "novo@x.com", Perfil.PESQUISADOR);
        when(encoder.encode(anyString())).thenReturn("hash");
        when(usuarioRepo.findByCpf("111")).thenReturn(Optional.empty());
        Usuario existente = criarUsuario(2L, "999", "novo@x.com", Perfil.PESQUISADOR, false);
        when(usuarioRepo.findByEmail("novo@x.com")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> usuarioService.create(dto))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Email");
    }

    // ---------- update ----------

    @Test
    void update_deveAtualizarDadosSemAlterarSenhaQuandoNaoInformada() {
        Usuario existente = criarUsuario(1L, "111", "a@a.com", Perfil.PESQUISADOR, false);
        existente.setSenha("senhaAntiga");
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepo.findByCpf("111")).thenReturn(Optional.of(existente));
        when(usuarioRepo.findByEmail("novo@a.com")).thenReturn(Optional.empty());
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setCpf("111");
        dto.setEmail("novo@a.com");
        dto.setNome("Nome Atualizado");
        dto.setPerfil(Perfil.PESQUISADOR);
        dto.setPodeCadastrar(false);

        Usuario atualizado = usuarioService.update(1L, dto);

        assertThat(atualizado.getNome()).isEqualTo("Nome Atualizado");
        assertThat(atualizado.getEmail()).isEqualTo("novo@a.com");
        assertThat(atualizado.getSenha()).isEqualTo("senhaAntiga");
        verify(encoder, never()).encode(anyString());
    }

    @Test
    void update_comNovaSenha_deveCodificarSenha() {
        Usuario existente = criarUsuario(1L, "111", "a@a.com", Perfil.PESQUISADOR, false);
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepo.findByCpf("111")).thenReturn(Optional.of(existente));
        when(usuarioRepo.findByEmail("a@a.com")).thenReturn(Optional.of(existente));
        when(encoder.encode("novaSenha")).thenReturn("hashNova");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setCpf("111");
        dto.setEmail("a@a.com");
        dto.setNome("Nome");
        dto.setPerfil(Perfil.PESQUISADOR);
        dto.setPodeCadastrar(false);
        dto.setSenha("novaSenha");

        Usuario atualizado = usuarioService.update(1L, dto);

        assertThat(atualizado.getSenha()).isEqualTo("hashNova");
    }

    // ---------- delete ----------

    @Test
    void delete_quandoExiste_removeUsuario() {
        Usuario u = criarUsuario(1L, "111", "a@a.com", Perfil.PESQUISADOR, false);
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(u));

        usuarioService.delete(1L);

        verify(usuarioRepo).delete(u);
    }

    @Test
    void delete_quandoNaoExiste_lancaObjectNotFoundException() {
        when(usuarioRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.delete(1L))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(usuarioRepo, never()).delete(any());
    }

    // ---------- recuperação de senha ----------

    @Test
    void generatePasswordResetToken_usuarioExiste_geraTokenEEnviaEmail() {
        Usuario u = criarUsuario(1L, "111", "a@a.com", Perfil.PESQUISADOR, false);
        when(usuarioRepo.findByEmail("a@a.com")).thenReturn(Optional.of(u));
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.generatePasswordResetToken("a@a.com", "http://localhost");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepo).save(captor.capture());
        assertThat(captor.getValue().getResetToken()).isNotBlank();
        assertThat(captor.getValue().getResetTokenExpiry()).isAfter(LocalDateTime.now());
        verify(emailService).sendPasswordResetEmail(eq("a@a.com"), anyString());
    }

    @Test
    void generatePasswordResetToken_usuarioNaoExiste_lancaObjectNotFoundException() {
        when(usuarioRepo.findByEmail("naoexiste@a.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.generatePasswordResetToken("naoexiste@a.com", "http://localhost"))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void generatePasswordResetToken_falhaNoEnvioDeEmail_naoPropagaExcecao() {
        Usuario u = criarUsuario(1L, "111", "a@a.com", Perfil.PESQUISADOR, false);
        when(usuarioRepo.findByEmail("a@a.com")).thenReturn(Optional.of(u));
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("SMTP indisponível"))
                .when(emailService).sendPasswordResetEmail(anyString(), anyString());

        usuarioService.generatePasswordResetToken("a@a.com", "http://localhost");

        verify(usuarioRepo).save(any(Usuario.class));
    }

    @Test
    void resetPassword_tokenValido_atualizaSenhaELimpaToken() {
        Usuario u = criarUsuario(1L, "111", "a@a.com", Perfil.PESQUISADOR, false);
        u.setResetToken("token-valido");
        u.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        when(usuarioRepo.findByResetToken("token-valido")).thenReturn(Optional.of(u));
        when(encoder.encode("novaSenha123")).thenReturn("hashNova");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.resetPassword("token-valido", "novaSenha123");

        assertThat(u.getSenha()).isEqualTo("hashNova");
        assertThat(u.getResetToken()).isNull();
        assertThat(u.getResetTokenExpiry()).isNull();
    }

    @Test
    void resetPassword_tokenInexistente_lancaObjectNotFoundException() {
        when(usuarioRepo.findByResetToken("invalido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.resetPassword("invalido", "novaSenha"))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void resetPassword_tokenExpirado_lancaAuthorizationException() {
        Usuario u = criarUsuario(1L, "111", "a@a.com", Perfil.PESQUISADOR, false);
        u.setResetToken("token-expirado");
        u.setResetTokenExpiry(LocalDateTime.now().minusMinutes(1));
        when(usuarioRepo.findByResetToken("token-expirado")).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> usuarioService.resetPassword("token-expirado", "novaSenha"))
                .isInstanceOf(AuthorizationException.class);

        verify(usuarioRepo, never()).save(any());
    }
}
