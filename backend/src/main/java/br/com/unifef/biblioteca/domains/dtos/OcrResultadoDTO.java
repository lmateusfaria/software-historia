package br.com.unifef.biblioteca.domains.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OcrResultadoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String textoCompleto;
    private List<String> pessoas = new ArrayList<>();
    private List<String> locais = new ArrayList<>();
    private List<String> eventos = new ArrayList<>();
    private List<String> organizacoes = new ArrayList<>();
    private List<String> assuntos = new ArrayList<>();
    private List<String> datasMencionadas = new ArrayList<>();
    private String tipoDocumento;

    public OcrResultadoDTO() {}

    // Getters e Setters
    public String getTextoCompleto() { return textoCompleto; }
    public void setTextoCompleto(String textoCompleto) { this.textoCompleto = textoCompleto; }

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

    public List<String> getDatasMencionadas() { return datasMencionadas; }
    public void setDatasMencionadas(List<String> datasMencionadas) { this.datasMencionadas = datasMencionadas; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
}
