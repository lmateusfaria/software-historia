package br.com.unifef.biblioteca.domains.graph;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.HashSet;
import java.util.Set;

@Node
public class Pessoa {

    @Id
    private String nome;

    private String descricao;

    @Relationship(type = "MENCIONADA_EM")
    private Set<DocumentoNode> documentos = new HashSet<>();

    public Pessoa() {}

    public Pessoa(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Set<DocumentoNode> getDocumentos() { return documentos; }
    public void mencaoEm(DocumentoNode doc) { this.documentos.add(doc); }
}
