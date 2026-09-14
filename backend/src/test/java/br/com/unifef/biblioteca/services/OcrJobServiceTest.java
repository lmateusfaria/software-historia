package br.com.unifef.biblioteca.services;

import br.com.unifef.biblioteca.domains.Documento;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import br.com.unifef.biblioteca.repositories.DocumentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o job de sincronização de OCR pendente.
 */
@ExtendWith(MockitoExtension.class)
class OcrJobServiceTest {

    @Mock
    private DocumentoRepository repository;

    @InjectMocks
    private OcrJobService ocrJobService;

    @Test
    void processPendingOcr_semDocumentosPendentes_naoRealizaNenhumaAlteracao() {
        when(repository.findByStatus(StatusDocumento.PENDENTE_OCR)).thenReturn(Collections.emptyList());

        ocrJobService.processPendingOcr();

        verify(repository, never()).save(any());
    }

    @Test
    void processPendingOcr_comDocumentoPendente_transicionaStatusEExtraiConteudo() {
        Documento doc = new Documento();
        doc.setId(1L);
        doc.setDescricao("Jornal Teste");
        doc.setStatus(StatusDocumento.PENDENTE_OCR);

        when(repository.findByStatus(StatusDocumento.PENDENTE_OCR)).thenReturn(List.of(doc));
        when(repository.save(any(Documento.class))).thenAnswer(inv -> inv.getArgument(0));

        ocrJobService.processPendingOcr();

        // repository.save() é chamado duas vezes (PROCESSANDO_OCR e depois AGUARDANDO_APROVACAO),
        // mas como é o mesmo objeto mutável, validamos o estado final e a quantidade de chamadas.
        verify(repository, times(2)).save(doc);
        assertThat(doc.getStatus()).isEqualTo(StatusDocumento.AGUARDANDO_APROVACAO);
        assertThat(doc.getConteudoOcr()).contains("Jornal Teste");
    }

    @Test
    void processPendingOcr_quandoUmDocumentoFalha_continuaProcessandoOsDemais() {
        Documento docComFalha = mock(Documento.class);
        when(docComFalha.getId()).thenReturn(1L);
        when(docComFalha.getDescricao()).thenThrow(new RuntimeException("Falha simulada"));

        Documento docOk = new Documento();
        docOk.setId(2L);
        docOk.setDescricao("Documento OK");
        docOk.setStatus(StatusDocumento.PENDENTE_OCR);

        when(repository.findByStatus(StatusDocumento.PENDENTE_OCR)).thenReturn(List.of(docComFalha, docOk));
        when(repository.save(any(Documento.class))).thenAnswer(inv -> inv.getArgument(0));

        ocrJobService.processPendingOcr();

        assertThat(docOk.getStatus()).isEqualTo(StatusDocumento.AGUARDANDO_APROVACAO);
    }
}
