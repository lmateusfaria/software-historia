package br.com.unifef.biblioteca.services;

import br.com.unifef.biblioteca.domains.dtos.OcrResultadoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GptOcrService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            Voce e um especialista em analise de documentos historicos brasileiros.
            Extraia as informacoes da imagem de documento fornecida, seguindo estas regras:
            1. Transcreva o texto de forma fiel e completa no campo textoCompleto.
            2. Se nao conseguir identificar algum campo, retorne uma lista vazia [] ou string vazia "".
            3. Seja preciso: nao invente informacoes que nao estejam visiveis na imagem.
            4. Para pessoas, use o nome completo quando visivel.
            5. Para assuntos, identifique de 1 a 5 temas principais do documento.
            6. Nunca repita a mesma frase ou trecho varias vezes seguidas.
            """;

    private static final String USER_PROMPT = "Analise esta imagem de documento historico e extraia os dados conforme as regras.";

    public GptOcrService(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.objectMapper = new ObjectMapper();
    }

    public OcrResultadoDTO extrairDadosImagem(byte[] imageBytes, String mimeType) {
        MimeType mime = MimeTypeUtils.parseMimeType(mimeType);
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes);
        Media imageMedia = new Media(mime, imageResource);

        UserMessage userMessage = UserMessage.builder()
                .text(USER_PROMPT)
                .media(List.of(imageMedia))
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(OpenAiApi.ChatModel.GPT_4_O_MINI.getValue())
                .temperature(0.1)
                .maxTokens(4096)
                .frequencyPenalty(0.3)
                .responseFormat(buildResponseFormat())
                .build();

        Instant inicio = Instant.now();
        ChatResponse response = chatModel.call(
                new Prompt(List.of(new SystemMessage(SYSTEM_PROMPT), userMessage), options));
        Duration duracao = Duration.between(inicio, Instant.now());

        Usage usage = response.getMetadata() != null ? response.getMetadata().getUsage() : null;
        if (usage != null) {
            log.info("OCR GPT-4o-mini concluido em {}ms - tokens prompt={} completion={} total={}",
                    duracao.toMillis(), usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
        } else {
            log.info("OCR GPT-4o-mini concluido em {}ms (uso de tokens indisponivel)", duracao.toMillis());
        }

        String content = response.getResult().getOutput().getText();
        OcrResultadoDTO dto = parseResponse(content);

        if (pareceLoopRepeticao(dto.getTextoCompleto())) {
            log.warn("Possivel loop de repeticao detectado no texto extraido (tamanho={})",
                    dto.getTextoCompleto() != null ? dto.getTextoCompleto().length() : 0);
        }

        return dto;
    }

    /**
     * Heuristica de deteccao de loop de repeticao do modelo: verifica se o final do texto
     * (onde o loop costuma acontecer, ate o corte por maxTokens) se repete varias vezes.
     */
    public boolean pareceLoopRepeticao(String texto) {
        if (texto == null || texto.length() < 240) {
            return false;
        }
        String amostra = texto.substring(texto.length() - 60);
        int ocorrencias = 0;
        int idx = 0;
        while ((idx = texto.indexOf(amostra, idx)) != -1) {
            ocorrencias++;
            idx += amostra.length();
        }
        return ocorrencias >= 4;
    }

    private ResponseFormat buildResponseFormat() {
        Map<String, Object> arrayDeStrings = Map.of(
                "type", "array",
                "items", Map.of("type", "string"));

        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.ofEntries(
                        Map.entry("textoCompleto", Map.of("type", "string")),
                        Map.entry("pessoas", arrayDeStrings),
                        Map.entry("locais", arrayDeStrings),
                        Map.entry("eventos", arrayDeStrings),
                        Map.entry("organizacoes", arrayDeStrings),
                        Map.entry("assuntos", arrayDeStrings),
                        Map.entry("datasMencionadas", arrayDeStrings),
                        Map.entry("tipoDocumento", Map.of("type", "string"))),
                "required", List.of("textoCompleto", "pessoas", "locais", "eventos",
                        "organizacoes", "assuntos", "datasMencionadas", "tipoDocumento"),
                "additionalProperties", false);

        return ResponseFormat.builder()
                .type(ResponseFormat.Type.JSON_SCHEMA)
                .jsonSchema(ResponseFormat.JsonSchema.builder()
                        .name("ocr_resultado")
                        .schema(schema)
                        .strict(true)
                        .build())
                .build();
    }

    @SuppressWarnings("unchecked")
    private OcrResultadoDTO parseResponse(String jsonContent) {
        OcrResultadoDTO dto = new OcrResultadoDTO();

        try {
            // Rede de seguranca: o modo JSON_SCHEMA estrito ja garante JSON valido na
            // grande maioria dos casos, mas mantemos a limpeza de markdown por seguranca.
            String cleaned = jsonContent.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("\\n?```$", "").trim();
            }

            Map<String, Object> map = objectMapper.readValue(cleaned, Map.class);

            dto.setTextoCompleto(getStringOrEmpty(map, "textoCompleto"));
            dto.setPessoas(getListOrEmpty(map, "pessoas"));
            dto.setLocais(getListOrEmpty(map, "locais"));
            dto.setEventos(getListOrEmpty(map, "eventos"));
            dto.setOrganizacoes(getListOrEmpty(map, "organizacoes"));
            dto.setAssuntos(getListOrEmpty(map, "assuntos"));
            dto.setDatasMencionadas(getListOrEmpty(map, "datasMencionadas"));
            dto.setTipoDocumento(getStringOrEmpty(map, "tipoDocumento"));

        } catch (Exception e) {
            log.error("Erro ao fazer parse da resposta OCR: {}", e.getMessage());
            dto.setTextoCompleto(jsonContent);
        }

        return dto;
    }

    private String getStringOrEmpty(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    @SuppressWarnings("unchecked")
    private List<String> getListOrEmpty(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) val) {
                if (item != null) result.add(item.toString());
            }
            return result;
        }
        return new ArrayList<>();
    }
}
