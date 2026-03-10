package br.com.unifef.biblioteca.domains.dtos;

import java.io.Serializable;

import br.com.unifef.biblioteca.domains.Documento;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
@NoArgsConstructor
public class DocumentoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String titulo;
    private String descricao;
    private String urlImagem;
    private String conteudoOcr;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataDigitalizacao;
    private StatusDocumento status;
    private Long usuarioId;
    private String usuarioNome;
    private String tipo;
    private Integer diaDocumento;
    private Integer mesDocumento;
    private Integer anoDocumento;
    private String localOrigem;
    private String edicao;
    private String marcadores;

    public DocumentoDTO(Documento obj) {
        this.id = obj.getId();
        this.titulo = obj.getTitulo();
        this.descricao = obj.getDescricao();
        this.urlImagem = obj.getUrlImagem();
        this.conteudoOcr = obj.getConteudoOcr();
        this.dataDigitalizacao = obj.getDataDigitalizacao();
        this.status = obj.getStatus();
        this.usuarioId = obj.getResponsavel().getId();
        this.usuarioNome = obj.getResponsavel().getNome();
        this.tipo = obj.getTipo();
        this.diaDocumento = obj.getDiaDocumento();
        this.mesDocumento = obj.getMesDocumento();
        this.anoDocumento = obj.getAnoDocumento();
        this.localOrigem = obj.getLocalOrigem();
        this.edicao = obj.getEdicao();
        this.marcadores = obj.getMarcadores();
    }
}
