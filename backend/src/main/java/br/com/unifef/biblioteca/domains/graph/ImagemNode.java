package br.com.unifef.biblioteca.domains.graph;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.HashSet;
import java.util.Set;

@Node
public class ImagemNode {

    @Id
    @GeneratedValue
    private Long neoId;

    private Long documentoId;
    private String imagemUrl;
    private Integer indice;
    private String textoExtraido;

    @Relationship(type = "MENCIONADA_EM", direction = Relationship.Direction.INCOMING)
    private Set<Pessoa> pessoas = new HashSet<>();

    @Relationship(type = "LOCALIZADO_EM", direction = Relationship.Direction.INCOMING)
    private Set<Local> locais = new HashSet<>();

    @Relationship(type = "OCORREU_EM", direction = Relationship.Direction.INCOMING)
    private Set<Evento> eventos = new HashSet<>();

    @Relationship(type = "PARTICIPA_DE", direction = Relationship.Direction.INCOMING)
    private Set<Organizacao> organizacoes = new HashSet<>();

    @Relationship(type = "TRATA_DE", direction = Relationship.Direction.INCOMING)
    private Set<Assunto> assuntos = new HashSet<>();

    public ImagemNode() {}

    public ImagemNode(Long documentoId, String imagemUrl, Integer indice) {
        this.documentoId = documentoId;
        this.imagemUrl = imagemUrl;
        this.indice = indice;
    }

    // Getters e Setters
    public Long getNeoId() { return neoId; }
    public void setNeoId(Long neoId) { this.neoId = neoId; }

    public Long getDocumentoId() { return documentoId; }
    public void setDocumentoId(Long documentoId) { this.documentoId = documentoId; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

    public Integer getIndice() { return indice; }
    public void setIndice(Integer indice) { this.indice = indice; }

    public String getTextoExtraido() { return textoExtraido; }
    public void setTextoExtraido(String textoExtraido) { this.textoExtraido = textoExtraido; }

    public Set<Pessoa> getPessoas() { return pessoas; }
    public void setPessoas(Set<Pessoa> pessoas) { this.pessoas = pessoas; }

    public Set<Local> getLocais() { return locais; }
    public void setLocais(Set<Local> locais) { this.locais = locais; }

    public Set<Evento> getEventos() { return eventos; }
    public void setEventos(Set<Evento> eventos) { this.eventos = eventos; }

    public Set<Organizacao> getOrganizacoes() { return organizacoes; }
    public void setOrganizacoes(Set<Organizacao> organizacoes) { this.organizacoes = organizacoes; }

    public Set<Assunto> getAssuntos() { return assuntos; }
    public void setAssuntos(Set<Assunto> assuntos) { this.assuntos = assuntos; }
}
