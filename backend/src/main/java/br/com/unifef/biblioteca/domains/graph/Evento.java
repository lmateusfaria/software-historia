package br.com.unifef.biblioteca.domains.graph;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.HashSet;
import java.util.Set;

@Node
public class Evento {
    @Id
    private String nome;
    private String dataOcorrencia;

    @Relationship(type = "OCORREU_EM")
    private Set<DocumentoNode> documentos = new HashSet<>();

    public Evento() {}
    public Evento(String nome) { this.nome = nome; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDataOcorrencia() { return dataOcorrencia; }
    public void setDataOcorrencia(String dataOcorrencia) { this.dataOcorrencia = dataOcorrencia; }
    public Set<DocumentoNode> getDocumentos() { return documentos; }
}
