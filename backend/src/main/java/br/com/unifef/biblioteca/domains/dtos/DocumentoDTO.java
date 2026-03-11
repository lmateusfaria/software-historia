package br.com.unifef.biblioteca.domains.dtos;

import java.io.Serializable;

import br.com.unifef.biblioteca.domains.Documento;
import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import br.com.unifef.biblioteca.domains.graph.DocumentoNode;
import br.com.unifef.biblioteca.domains.graph.Pessoa;
import br.com.unifef.biblioteca.domains.graph.Local;
import br.com.unifef.biblioteca.domains.graph.Evento;
import br.com.unifef.biblioteca.domains.graph.Organizacao;

public class DocumentoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
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
    private List<String> imagensUrls = new ArrayList<>();
    private List<String> pessoas = new ArrayList<>();
    private List<String> locais = new ArrayList<>();
    private List<String> eventos = new ArrayList<>();
    private List<String> organizacoes = new ArrayList<>();

    public DocumentoDTO() {
    }

    public DocumentoDTO(Documento obj) {
        this.id = obj.getId();
        this.descricao = obj.getDescricao();
        this.urlImagem = obj.getUrlImagem() != null ? "/api/documentos/download/" + obj.getUrlImagem() : null;
        this.conteudoOcr = obj.getConteudoOcr();
        this.dataDigitalizacao = obj.getDataDigitalizacao();
        this.status = obj.getStatus();
        this.usuarioId = obj.getResponsavel() != null ? obj.getResponsavel().getId() : null;
        this.usuarioNome = obj.getResponsavel() != null ? obj.getResponsavel().getNome() : null;
        this.tipo = obj.getTipo();
        this.diaDocumento = obj.getDiaDocumento();
        this.mesDocumento = obj.getMesDocumento();
        this.anoDocumento = obj.getAnoDocumento();
        this.localOrigem = obj.getLocalOrigem();
        this.edicao = obj.getEdicao();
        this.marcadores = obj.getMarcadores();
        
        if (obj.getImagensUrls() != null) {
            this.imagensUrls = obj.getImagensUrls().stream()
                .map(img -> "/api/documentos/download/" + img)
                .collect(Collectors.toList());
        }
    }

    public DocumentoDTO(Documento obj, DocumentoNode node) {
        this(obj);
        if (node != null) {
            if (node.getPessoas() != null) {
                this.pessoas = node.getPessoas().stream().map(Pessoa::getNome).collect(Collectors.toList());
            }
            if (node.getLocais() != null) {
                this.locais = node.getLocais().stream().map(Local::getNome).collect(Collectors.toList());
            }
            if (node.getEventos() != null) {
                this.eventos = node.getEventos().stream().map(Evento::getNome).collect(Collectors.toList());
            }
            if (node.getOrganizacoes() != null) {
                this.organizacoes = node.getOrganizacoes().stream().map(Organizacao::getNome).collect(Collectors.toList());
            }
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getUrlImagem() { return urlImagem; }
    public void setUrlImagem(String urlImagem) { this.urlImagem = urlImagem; }

    public String getConteudoOcr() { return conteudoOcr; }
    public void setConteudoOcr(String conteudoOcr) { this.conteudoOcr = conteudoOcr; }

    public LocalDate getDataDigitalizacao() { return dataDigitalizacao; }
    public void setDataDigitalizacao(LocalDate dataDigitalizacao) { this.dataDigitalizacao = dataDigitalizacao; }

    public StatusDocumento getStatus() { return status; }
    public void setStatus(StatusDocumento status) { this.status = status; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getDiaDocumento() { return diaDocumento; }
    public void setDiaDocumento(Integer diaDocumento) { this.diaDocumento = diaDocumento; }

    public Integer getMesDocumento() { return mesDocumento; }
    public void setMesDocumento(Integer mesDocumento) { this.mesDocumento = mesDocumento; }

    public Integer getAnoDocumento() { return anoDocumento; }
    public void setAnoDocumento(Integer anoDocumento) { this.anoDocumento = anoDocumento; }

    public String getLocalOrigem() { return localOrigem; }
    public void setLocalOrigem(String localOrigem) { this.localOrigem = localOrigem; }

    public String getEdicao() { return edicao; }
    public void setEdicao(String edicao) { this.edicao = edicao; }

    public String getMarcadores() { return marcadores; }
    public void setMarcadores(String marcadores) { this.marcadores = marcadores; }

    public List<String> getImagensUrls() { return imagensUrls; }
    public void setImagensUrls(List<String> imagensUrls) { this.imagensUrls = imagensUrls; }

    public List<String> getPessoas() { return pessoas; }
    public void setPessoas(List<String> pessoas) { this.pessoas = pessoas; }

    public List<String> getLocais() { return locais; }
    public void setLocais(List<String> locais) { this.locais = locais; }

    public List<String> getEventos() { return eventos; }
    public void setEventos(List<String> eventos) { this.eventos = eventos; }

    public List<String> getOrganizacoes() { return organizacoes; }
    public void setOrganizacoes(List<String> organizacoes) { this.organizacoes = organizacoes; }
}
