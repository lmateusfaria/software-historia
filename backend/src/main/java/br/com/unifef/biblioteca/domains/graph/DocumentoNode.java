package br.com.unifef.biblioteca.domains.graph;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.HashSet;
import java.util.Set;

@Node
public class DocumentoNode {

    @Id
    private Long id; // ID correspondente ao PostgreSQL

    private String titulo;

    @Relationship(type = "MENCIONADA_EM", direction = Relationship.Direction.INCOMING)
    private Set<Pessoa> pessoas = new HashSet<>();

    @Relationship(type = "LOCALIZADO_EM", direction = Relationship.Direction.INCOMING)
    private Set<Local> locais = new HashSet<>();

    @Relationship(type = "OCORREU_EM", direction = Relationship.Direction.INCOMING)
    private Set<Evento> eventos = new HashSet<>();

    @Relationship(type = "PARTICIPA_DE", direction = Relationship.Direction.INCOMING)
    private Set<Organizacao> organizacoes = new HashSet<>();

    public DocumentoNode() {}

    public DocumentoNode(Long id, String titulo) {
        this.id = id;
        this.titulo = titulo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Set<Pessoa> getPessoas() { return pessoas; }
    public void setPessoas(Set<Pessoa> pessoas) { this.pessoas = pessoas; }

    public Set<Local> getLocais() { return locais; }
    public void setLocais(Set<Local> locais) { this.locais = locais; }

    public Set<Evento> getEventos() { return eventos; }
    public void setEventos(Set<Evento> eventos) { this.eventos = eventos; }

    public Set<Organizacao> getOrganizacoes() { return organizacoes; }
    public void setOrganizacoes(Set<Organizacao> organizacoes) { this.organizacoes = organizacoes; }
}
