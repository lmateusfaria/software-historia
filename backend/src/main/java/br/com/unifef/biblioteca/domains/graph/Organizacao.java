package br.com.unifef.biblioteca.domains.graph;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.HashSet;
import java.util.Set;

@Node
public class Organizacao {
    @Id
    private String nome;

    @Relationship(type = "PARTICIPA_DE")
    private Set<DocumentoNode> documentos = new HashSet<>();

    public Organizacao() {}
    public Organizacao(String nome) { this.nome = nome; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Set<DocumentoNode> getDocumentos() { return documentos; }
}
