package br.com.unifef.biblioteca.domains.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrStatusDTO {
    private String status;
    private String mensagem;
    private Long documentoId;
    private String imagemUrl;

    public static OcrStatusDTO processando(Long documentoId, String imagemUrl) {
        return new OcrStatusDTO("PROCESSANDO", "A extração de dados com IA foi iniciada em segundo plano. Os resultados aparecerão no acervo em instantes.", documentoId, imagemUrl);
    }
}
