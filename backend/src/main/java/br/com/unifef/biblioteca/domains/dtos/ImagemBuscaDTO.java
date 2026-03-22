package br.com.unifef.biblioteca.domains.dtos;

import br.com.unifef.biblioteca.domains.graph.ImagemNode;
import br.com.unifef.biblioteca.domains.graph.Assunto;
import br.com.unifef.biblioteca.domains.graph.Pessoa;
import br.com.unifef.biblioteca.domains.graph.Local;
import br.com.unifef.biblioteca.domains.graph.Evento;
import br.com.unifef.biblioteca.domains.graph.Organizacao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImagemBuscaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long documentoId;
    private String imagemUrl;
    private String urlThumbnail;
    private String urlPreview;
    private Integer indice;
    private String textoExtraido;
    private List<String> pessoas = new ArrayList<>();
    private List<String> locais = new ArrayList<>();
    private List<String> eventos = new ArrayList<>();
    private List<String> organizacoes = new ArrayList<>();
    private List<String> assuntos = new ArrayList<>();

    public ImagemBuscaDTO() {
    }

    public ImagemBuscaDTO(ImagemNode node) {
        this.documentoId = node.getDocumentoId();
        this.imagemUrl = "/api/documentos/download/" + node.getImagemUrl();
        this.indice = node.getIndice();
        this.textoExtraido = node.getTextoExtraido();
        
        String originalName = node.getImagemUrl();
        String baseName = originalName.replaceAll("(?i)\\.(jpg|jpeg|png|heic|pdf)$", "");
        this.urlThumbnail = "/api/documentos/download/" + baseName + "_thumb.jpg";
        this.urlPreview = "/api/documentos/download/" + baseName + "_preview.jpg";

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
        if (node.getAssuntos() != null) {
            this.assuntos = node.getAssuntos().stream().map(Assunto::getNome).collect(Collectors.toList());
        }
    }

    // Getters e Setters
    public Long getDocumentoId() { return documentoId; }
    public void setDocumentoId(Long documentoId) { this.documentoId = documentoId; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

    public String getUrlThumbnail() { return urlThumbnail; }
    public void setUrlThumbnail(String urlThumbnail) { this.urlThumbnail = urlThumbnail; }

    public String getUrlPreview() { return urlPreview; }
    public void setUrlPreview(String urlPreview) { this.urlPreview = urlPreview; }

    public Integer getIndice() { return indice; }
    public void setIndice(Integer indice) { this.indice = indice; }

    public String getTextoExtraido() { return textoExtraido; }
    public void setTextoExtraido(String textoExtraido) { this.textoExtraido = textoExtraido; }

    public List<String> getPessoas() { return pessoas; }
    public void setPessoas(List<String> pessoas) { this.pessoas = pessoas; }

    public List<String> getLocais() { return locais; }
    public void setLocais(List<String> locais) { this.locais = locais; }

    public List<String> getEventos() { return eventos; }
    public void setEventos(List<String> eventos) { this.eventos = eventos; }

    public List<String> getOrganizacoes() { return organizacoes; }
    public void setOrganizacoes(List<String> organizacoes) { this.organizacoes = organizacoes; }

    public List<String> getAssuntos() { return assuntos; }
    public void setAssuntos(List<String> assuntos) { this.assuntos = assuntos; }
}
