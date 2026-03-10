package br.com.unifef.biblioteca.domains.graph;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
public class DocumentoNode {

    @Id
    private Long id; // ID correspondente ao PostgreSQL

    private String titulo;

    public DocumentoNode() {}

    public DocumentoNode(Long id, String titulo) {
        this.id = id;
        this.titulo = titulo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
}
