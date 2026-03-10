package br.com.unifef.biblioteca.domains.graph;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.HashSet;
import java.util.Set;

@Node
public class Local {

    @Id
    private String nome;

    private Double latitude;
    private Double longitude;

    @Relationship(type = "LOCALIZADO_EM")
    private Set<DocumentoNode> documentos = new HashSet<>();

    public Local() {}

    public Local(String nome) {
        this.nome = nome;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Set<DocumentoNode> getDocumentos() { return documentos; }
    public void vinculadoA(DocumentoNode doc) { this.documentos.add(doc); }
}
