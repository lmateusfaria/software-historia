package br.com.unifef.biblioteca.services;

import br.com.unifef.biblioteca.domains.Documento;
import br.com.unifef.biblioteca.domains.ImagemOcrResultado;
import br.com.unifef.biblioteca.domains.dtos.DocumentoDTO;
import br.com.unifef.biblioteca.domains.dtos.OcrResultadoUpdateDTO;
import br.com.unifef.biblioteca.domains.dtos.OcrStatusDTO;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import br.com.unifef.biblioteca.domains.graph.ImagemNode;
import br.com.unifef.biblioteca.repositories.DocumentoRepository;
import br.com.unifef.biblioteca.repositories.ImagemOcrRepository;
import br.com.unifef.biblioteca.repositories.UsuarioRepository;
import br.com.unifef.biblioteca.repositories.graph.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link DocumentoService}, focados nas regras de negócio
 * puras (visibilidade pública x autenticada, aprovação, exclusão e edição de
 * resultados de OCR), sem exercitar os fluxos assíncronos de upload/processamento
 * de imagem que dependem de I/O real (MinIO, PDFBox, GPT).
 */
@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private DocumentoRepository repository;
    @Mock private FileStorageService fileStorageService;
    @Mock private DocumentoNodeRepository documentoNodeRepository;
    @Mock private GptOcrService gptOcrService;
    @Mock private ImagemOcrRepository imagemOcrRepository;
    @Mock private ImagemNodeRepository imagemNodeRepository;
    @Mock private PessoaRepository pessoaRepository;
    @Mock private LocalRepository localRepository;
    @Mock private EventoRepository eventoRepository;
    @Mock private OrganizacaoRepository organizacaoRepository;
    @Mock private AssuntoRepository assuntoRepository;

    @InjectMocks
    private DocumentoService documentoService;

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String email, String role) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, authorities));
    }

    private void autenticarComoAnonimo() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    }

    private Documento criarDocumento(Long id, StatusDocumento status) {
        Documento doc = new Documento();
        doc.setId(id);
        doc.setDescricao("Documento " + id);
        doc.setStatus(status);
        return doc;
    }

    // ---------- findAll: visibilidade pública x autenticada ----------

    @Test
    void findAll_usuarioAnonimo_retornaApenasDocumentosAprovados() {
        autenticarComoAnonimo();
        when(repository.findByStatus(StatusDocumento.APROVADO))
                .thenReturn(List.of(criarDocumento(1L, StatusDocumento.APROVADO)));

        List<DocumentoDTO> resultado = documentoService.findAll();

        assertThat(resultado).hasSize(1);
        verify(repository).findByStatus(StatusDocumento.APROVADO);
        verify(repository, never()).findAll();
    }

    @Test
    void findAll_semAutenticacao_tratadoComoAnonimo() {
        // Nenhuma autenticação setada no contexto (cenário de requisição pública real)
        when(repository.findByStatus(StatusDocumento.APROVADO)).thenReturn(Collections.emptyList());

        documentoService.findAll();

        verify(repository).findByStatus(StatusDocumento.APROVADO);
    }

    @Test
    void findAll_usuarioAutenticado_retornaTodosOsDocumentos() {
        autenticarComo("prof@x.com", "ROLE_PROFESSOR");
        when(repository.findAll()).thenReturn(List.of(
                criarDocumento(1L, StatusDocumento.APROVADO),
                criarDocumento(2L, StatusDocumento.AGUARDANDO_APROVACAO)));

        List<DocumentoDTO> resultado = documentoService.findAll();

        assertThat(resultado).hasSize(2);
        verify(repository, never()).findByStatus(any());
    }

    // ---------- findById: bloqueio de documentos não aprovados para anônimos ----------

    @Test
    void findById_anonimoConsultandoDocumentoAprovado_retornaDocumento() {
        autenticarComoAnonimo();
        Documento doc = criarDocumento(1L, StatusDocumento.APROVADO);
        when(repository.findById(1L)).thenReturn(Optional.of(doc));
        when(documentoNodeRepository.findById(1L)).thenReturn(Optional.empty());
        when(imagemOcrRepository.findByDocumentoIdOrderByIndice(1L)).thenReturn(Collections.emptyList());

        DocumentoDTO dto = documentoService.findById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    void findById_anonimoConsultandoDocumentoNaoAprovado_lancaExcecao() {
        autenticarComoAnonimo();
        Documento doc = criarDocumento(1L, StatusDocumento.AGUARDANDO_APROVACAO);
        when(repository.findById(1L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> documentoService.findById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    void findById_usuarioAutenticadoConsultandoDocumentoNaoAprovado_retornaDocumento() {
        autenticarComo("prof@x.com", "ROLE_PROFESSOR");
        Documento doc = criarDocumento(1L, StatusDocumento.AGUARDANDO_APROVACAO);
        when(repository.findById(1L)).thenReturn(Optional.of(doc));
        when(documentoNodeRepository.findById(1L)).thenReturn(Optional.empty());
        when(imagemOcrRepository.findByDocumentoIdOrderByIndice(1L)).thenReturn(Collections.emptyList());

        DocumentoDTO dto = documentoService.findById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    void findById_idInexistente_lancaExcecao() {
        autenticarComo("prof@x.com", "ROLE_PROFESSOR");
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.findById(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void findById_falhaAoConsultarNeo4j_naoInterrompeConsultaDoDocumento() {
        autenticarComo("prof@x.com", "ROLE_PROFESSOR");
        Documento doc = criarDocumento(1L, StatusDocumento.APROVADO);
        when(repository.findById(1L)).thenReturn(Optional.of(doc));
        when(documentoNodeRepository.findById(1L)).thenThrow(new RuntimeException("Neo4j indisponível"));
        when(imagemOcrRepository.findByDocumentoIdOrderByIndice(1L)).thenReturn(Collections.emptyList());

        DocumentoDTO dto = documentoService.findById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
    }

    // ---------- approve ----------

    @Test
    void approve_documentoExistente_alteraStatusParaAprovado() {
        Documento doc = criarDocumento(1L, StatusDocumento.AGUARDANDO_APROVACAO);
        when(repository.findById(1L)).thenReturn(Optional.of(doc));
        when(repository.save(any(Documento.class))).thenAnswer(inv -> inv.getArgument(0));

        DocumentoDTO dto = documentoService.approve(1L);

        assertThat(dto.getStatus()).isEqualTo(StatusDocumento.APROVADO);
    }

    @Test
    void approve_documentoInexistente_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.approve(99L))
                .isInstanceOf(RuntimeException.class);
    }

    // ---------- delete ----------

    @Test
    void delete_documentoComArquivos_removeArquivosNoMinioENoBanco() {
        Documento doc = criarDocumento(1L, StatusDocumento.APROVADO);
        doc.setImagensUrls(List.of("img1.jpg"));
        doc.setThumbnailsUrls(List.of("img1_thumb.jpg"));
        doc.setPreviewsUrls(List.of("img1_preview.jpg"));
        when(repository.findById(1L)).thenReturn(Optional.of(doc));

        documentoService.delete(1L);

        verify(fileStorageService).delete("img1.jpg");
        verify(fileStorageService).delete("img1_thumb.jpg");
        verify(fileStorageService).delete("img1_preview.jpg");
        verify(documentoNodeRepository).deleteById(1L);
        verify(repository).delete(doc);
    }

    @Test
    void delete_falhaAoRemoverArquivoNoMinio_naoImpedeExclusaoDoRegistro() {
        Documento doc = criarDocumento(1L, StatusDocumento.APROVADO);
        doc.setImagensUrls(List.of("img1.jpg"));
        when(repository.findById(1L)).thenReturn(Optional.of(doc));
        doThrow(new RuntimeException("Erro MinIO")).when(fileStorageService).delete("img1.jpg");

        documentoService.delete(1L);

        verify(repository).delete(doc);
    }

    @Test
    void delete_documentoInexistente_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.delete(99L))
                .isInstanceOf(RuntimeException.class);

        verify(repository, never()).delete(any());
    }

    // ---------- atualizarOcrResultado ----------

    @Test
    void atualizarOcrResultado_dadosValidos_atualizaTextoEEntidades() {
        ImagemOcrResultado entity = new ImagemOcrResultado();
        entity.setId(10L);
        entity.setDocumentoId(1L);
        entity.setImagemUrl("img1.jpg");
        when(imagemOcrRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(imagemOcrRepository.save(any(ImagemOcrResultado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(imagemNodeRepository.findByDocumentoId(1L)).thenReturn(List.of());
        when(repository.findById(1L)).thenReturn(Optional.of(criarDocumento(1L, StatusDocumento.APROVADO)));
        when(imagemNodeRepository.save(any(ImagemNode.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pessoaRepository.findById(anyString())).thenReturn(Optional.empty());

        OcrResultadoUpdateDTO dto = new OcrResultadoUpdateDTO();
        dto.setTextoCompleto("Texto corrigido manualmente");
        dto.setPessoas(List.of("Maria"));
        dto.setLocais(List.of());
        dto.setEventos(List.of());
        dto.setOrganizacoes(List.of());
        dto.setAssuntos(List.of());

        var resultado = documentoService.atualizarOcrResultado(1L, 10L, dto);

        assertThat(resultado.getTextoCompleto()).isEqualTo("Texto corrigido manualmente");
        assertThat(entity.getTextoExtraido()).isEqualTo("Texto corrigido manualmente");
        verify(imagemOcrRepository).save(entity);
    }

    @Test
    void atualizarOcrResultado_ocrNaoPertenceAoDocumento_lancaExcecao() {
        ImagemOcrResultado entity = new ImagemOcrResultado();
        entity.setId(10L);
        entity.setDocumentoId(2L); // pertence a outro documento
        when(imagemOcrRepository.findById(10L)).thenReturn(Optional.of(entity));

        OcrResultadoUpdateDTO dto = new OcrResultadoUpdateDTO();

        assertThatThrownBy(() -> documentoService.atualizarOcrResultado(1L, 10L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não pertence");

        verify(imagemOcrRepository, never()).save(any());
    }

    @Test
    void atualizarOcrResultado_idInexistente_lancaExcecao() {
        when(imagemOcrRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.atualizarOcrResultado(1L, 999L, new OcrResultadoUpdateDTO()))
                .isInstanceOf(RuntimeException.class);
    }

    // ---------- excluirOcrResultado ----------

    @Test
    void excluirOcrResultado_removeRegistroEZeraImagemNode() {
        ImagemOcrResultado entity = new ImagemOcrResultado();
        entity.setId(10L);
        entity.setDocumentoId(1L);
        entity.setImagemUrl("img1.jpg");
        when(imagemOcrRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(imagemOcrRepository.findByDocumentoIdOrderByIndice(1L)).thenReturn(List.of(entity));

        ImagemNode node = new ImagemNode(1L, "img1.jpg", 0);
        node.setTextoExtraido("texto antigo");
        when(imagemNodeRepository.findByDocumentoId(1L)).thenReturn(List.of(node));

        documentoService.excluirOcrResultado(1L, 10L);

        verify(imagemOcrRepository).deleteAll(List.of(entity));
        verify(imagemNodeRepository).save(node);
        assertThat(node.getTextoExtraido()).isNull();
    }

    @Test
    void excluirOcrResultado_ocrNaoPertenceAoDocumento_lancaExcecao() {
        ImagemOcrResultado entity = new ImagemOcrResultado();
        entity.setId(10L);
        entity.setDocumentoId(2L);
        when(imagemOcrRepository.findById(10L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> documentoService.excluirOcrResultado(1L, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não pertence");
    }

    // ---------- processarOcrTodasPaginas: cálculo de páginas pendentes ----------

    @Test
    void processarOcrTodasPaginas_calculaCorretamenteAsPaginasPendentes() {
        ReflectionTestUtils.setField(documentoService, "self", documentoService);
        Documento doc = criarDocumento(1L, StatusDocumento.PENDENTE_OCR);
        doc.setImagensUrls(List.of("img1.jpg", "img2.jpg", "img3.jpg"));
        when(repository.findById(1L)).thenReturn(Optional.of(doc));

        ImagemOcrResultado jaProcessada = new ImagemOcrResultado();
        jaProcessada.setImagemUrl("img1.jpg");
        when(imagemOcrRepository.findByDocumentoIdOrderByIndice(1L)).thenReturn(List.of(jaProcessada));

        OcrStatusDTO status = documentoService.processarOcrTodasPaginas(1L);

        assertThat(status.getStatus()).isEqualTo("PROCESSANDO");
        assertThat(status.getMensagem()).contains("2 página(s)");
    }

    @Test
    void processarOcrTodasPaginas_documentoInexistente_lancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.processarOcrTodasPaginas(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void processarOcrTodasPaginas_todasPaginasJaProcessadas_zeroPendentes() {
        ReflectionTestUtils.setField(documentoService, "self", documentoService);
        Documento doc = criarDocumento(1L, StatusDocumento.PENDENTE_OCR);
        doc.setImagensUrls(List.of("img1.jpg"));
        when(repository.findById(1L)).thenReturn(Optional.of(doc));

        ImagemOcrResultado jaProcessada = new ImagemOcrResultado();
        jaProcessada.setImagemUrl("img1.jpg");
        when(imagemOcrRepository.findByDocumentoIdOrderByIndice(1L)).thenReturn(List.of(jaProcessada));

        OcrStatusDTO status = documentoService.processarOcrTodasPaginas(1L);

        assertThat(status.getMensagem()).contains("0 página(s)");
    }
}
