package br.com.unifef.biblioteca.services;

import br.com.unifef.biblioteca.domains.dtos.OcrResultadoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.MimeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GptOcrService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            Voce e um especialista em analise de documentos historicos brasileiros.
            Analise esta imagem de documento e extraia as seguintes informacoes em formato JSON valido:
            {
              "textoCompleto": "transcricao fiel e completa do texto visivel na imagem",
              "pessoas": ["nomes completos de pessoas mencionadas no texto"],
              "locais": ["nomes de cidades, estados, enderecos, bairros ou regioes mencionados"],
              "eventos": ["eventos historicos, acontecimentos, cerimonias ou fatos relatados"],
              "organizacoes": ["nomes de instituicoes, empresas, orgaos publicos, partidos, igrejas mencionados"],
              "assuntos": ["temas ou assuntos principais tratados no documento, ex: politica, educacao, saude"],
              "datasMencionadas": ["datas encontradas no documento, no formato original em que aparecem"],
              "tipoDocumento": "classificacao do tipo de documento (ex: certidao, jornal, ata, carta, decreto, fotografia)"
            }
            
            REGRAS IMPORTANTES:
            1. Responda APENAS com o JSON puro, sem marcacao markdown, sem ```json, sem explicacoes.
            2. Se nao conseguir identificar algum campo, retorne uma lista vazia [] ou string vazia "".
            3. Seja preciso: nao invente informacoes que nao estejam visiveis na imagem.
            4. Para pessoas, use o nome completo quando visivel.
            5. Para assuntos, identifique de 1 a 5 temas principais do documento.
            """;

    public GptOcrService(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.objectMapper = new ObjectMapper();
    }

    public OcrResultadoDTO extrairDadosImagem(byte[] imageBytes, String mimeType) {
        MimeType mime = MimeTypeUtils.parseMimeType(mimeType);
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes);
        Media imageMedia = new Media(mime, imageResource);

        UserMessage userMessage = UserMessage.builder()
                .text(SYSTEM_PROMPT)
                .media(List.of(imageMedia))
                .build();

        ChatResponse response = chatModel.call(
                new Prompt(userMessage,
                        OpenAiChatOptions.builder()
                                .model(OpenAiApi.ChatModel.GPT_4_O_MINI.getValue())
                                .temperature(0.1)
                                .build()));

        String content = response.getResult().getOutput().getText();

        return parseResponse(content);
    }

    @SuppressWarnings("unchecked")
    private OcrResultadoDTO parseResponse(String jsonContent) {
        OcrResultadoDTO dto = new OcrResultadoDTO();

        try {
            // Limpar possíveis marcações markdown
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
            System.err.println("Erro ao fazer parse da resposta OCR: " + e.getMessage());
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
