package br.com.unifef.biblioteca.domains.graph;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.HashSet;
import java.util.Set;

@Node
public class Assunto {

    @Id
    private String nome;

    @Relationship(type = "TRATA_DE")
    private Set<ImagemNode> imagens = new HashSet<>();

    public Assunto() {}

    public Assunto(String nome) {
        this.nome = nome;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Set<ImagemNode> getImagens() { return imagens; }
    public void vinculadoA(ImagemNode img) { this.imagens.add(img); }
}
