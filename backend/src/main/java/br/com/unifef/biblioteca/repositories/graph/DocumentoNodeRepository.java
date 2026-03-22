package br.com.unifef.biblioteca.repositories.graph;

import br.com.unifef.biblioteca.domains.graph.DocumentoNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentoNodeRepository extends Neo4jRepository<DocumentoNode, Long> {
    void deleteById(Long id);

    @Query("MATCH (d:DocumentoNode) " +
           "OPTIONAL MATCH (d)<-[:MENCIONADA_EM|LOCALIZADO_EM|OCORREU_EM|PARTICIPA_DE]-(e) " +
           "WHERE (e.nome IS NOT NULL AND toLower(e.nome) CONTAINS toLower($termo)) OR (d.titulo IS NOT NULL AND toLower(d.titulo) CONTAINS toLower($termo)) " +
           "RETURN d")
    List<DocumentoNode> findByEntidadeNome(String termo);

    @Query("MATCH (i:ImagemNode)<-[:MENCIONADA_EM|LOCALIZADO_EM|OCORREU_EM|PARTICIPA_DE|TRATA_DE]-(e) " +
           "WHERE toLower(e.nome) CONTAINS toLower($termo) " +
           "RETURN DISTINCT i.documentoId")
    List<Long> findDocumentIdsByImagemEntidadeNome(String termo);
}
