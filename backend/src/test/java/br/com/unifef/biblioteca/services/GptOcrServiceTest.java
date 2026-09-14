package br.com.unifef.biblioteca.services;

import br.com.unifef.biblioteca.domains.dtos.OcrResultadoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link GptOcrService}: heurística de detecção de loop de
 * repetição do modelo e parsing da resposta JSON estruturada retornada pelo GPT-4o-mini.
 */
@ExtendWith(MockitoExtension.class)
class GptOcrServiceTest {

    @Mock
    private ChatModel chatModel;

    private GptOcrService gptOcrService;

    private GptOcrService service() {
        if (gptOcrService == null) {
            gptOcrService = new GptOcrService(chatModel);
        }
        return gptOcrService;
    }

    // ---------- pareceLoopRepeticao ----------

    @Test
    void pareceLoopRepeticao_textoCurto_retornaFalse() {
        boolean resultado = service().pareceLoopRepeticao("Texto curto qualquer.");
        assertThat(resultado).isFalse();
    }

    @Test
    void pareceLoopRepeticao_textoNulo_retornaFalse() {
        assertThat(service().pareceLoopRepeticao(null)).isFalse();
    }

    @Test
    void pareceLoopRepeticao_textoLongoSemRepeticao_retornaFalse() {
        String texto = "Ata da reuniao ordinaria realizada em vinte de marco de mil novecentos e "
                + "cinquenta e dois, presentes os membros da diretoria e demais associados convocados "
                + "para deliberar sobre assuntos financeiros e administrativos da instituicao conforme edital.";
        assertThat(texto.length()).isGreaterThanOrEqualTo(240);

        assertThat(service().pareceLoopRepeticao(texto)).isFalse();
    }

    @Test
    void pareceLoopRepeticao_finalRepetidoVariasVezes_retornaTrue() {
        String trecho = "texto repetido sem sentido travado em loop infinito do modelo ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(trecho);
        }
        String texto = sb.toString();
        assertThat(texto.length()).isGreaterThanOrEqualTo(240);

        assertThat(service().pareceLoopRepeticao(texto)).isTrue();
    }

    // ---------- extrairDadosImagem (parsing da resposta) ----------

    @Test
    void extrairDadosImagem_respostaJsonValida_preencheTodosOsCampos() {
        String json = "{"
                + "\"textoCompleto\":\"Certidao de nascimento de Joao da Silva\","
                + "\"pessoas\":[\"Joao da Silva\"],"
                + "\"locais\":[\"Feira de Santana\"],"
                + "\"eventos\":[\"Nascimento\"],"
                + "\"organizacoes\":[\"Cartorio Central\"],"
                + "\"assuntos\":[\"Registro Civil\"],"
                + "\"datasMencionadas\":[\"10/03/1950\"],"
                + "\"tipoDocumento\":\"Certidao\""
                + "}";
        mockarRespostaChatModel(json);

        OcrResultadoDTO resultado = service().extrairDadosImagem(new byte[] {1, 2, 3}, "image/jpeg");

        assertThat(resultado.getTextoCompleto()).isEqualTo("Certidao de nascimento de Joao da Silva");
        assertThat(resultado.getPessoas()).containsExactly("Joao da Silva");
        assertThat(resultado.getLocais()).containsExactly("Feira de Santana");
        assertThat(resultado.getTipoDocumento()).isEqualTo("Certidao");
    }

    @Test
    void extrairDadosImagem_respostaComCercaMarkdown_removeCercaAntesDeFazerParse() {
        String json = "```json\n{\"textoCompleto\":\"abc\",\"pessoas\":[],\"locais\":[],\"eventos\":[],"
                + "\"organizacoes\":[],\"assuntos\":[],\"datasMencionadas\":[],\"tipoDocumento\":\"Outro\"}\n```";
        mockarRespostaChatModel(json);

        OcrResultadoDTO resultado = service().extrairDadosImagem(new byte[] {1}, "image/jpeg");

        assertThat(resultado.getTextoCompleto()).isEqualTo("abc");
        assertThat(resultado.getTipoDocumento()).isEqualTo("Outro");
    }

    @Test
    void extrairDadosImagem_respostaJsonInvalida_naoLancaExcecaoEDevolveTextoBruto() {
        String jsonInvalido = "isto nao e um json";
        mockarRespostaChatModel(jsonInvalido);

        OcrResultadoDTO resultado = service().extrairDadosImagem(new byte[] {1}, "image/jpeg");

        assertThat(resultado.getTextoCompleto()).isEqualTo(jsonInvalido);
        assertThat(resultado.getPessoas()).isEmpty();
    }

    private void mockarRespostaChatModel(String textoResposta) {
        AssistantMessage assistantMessage = new AssistantMessage(textoResposta);
        Generation generation = new Generation(assistantMessage);
        ChatResponse response = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
    }
}
