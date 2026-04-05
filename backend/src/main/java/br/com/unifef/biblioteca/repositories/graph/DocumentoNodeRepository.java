package br.com.unifef.biblioteca.repositories.graph;

import br.com.unifef.biblioteca.domains.graph.DocumentoNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentoNodeRepository extends Neo4jRepository<DocumentoNode, Long> {
    void deleteById(Long id);

    @Query("CALL { " +
           "  MATCH (d:DocumentoNode) WHERE d.titulo IS NOT NULL AND toLower(d.titulo) CONTAINS toLower($termo) RETURN d " +
           "  UNION " +
           "  MATCH (e)-[:MENCIONADA_EM|LOCALIZADO_EM|OCORREU_EM|PARTICIPA_DE]->(d:DocumentoNode) " +
           "  WHERE toLower(e.nome) CONTAINS toLower($termo) RETURN d " +
           "} RETURN d")
    List<DocumentoNode> findByEntidadeNome(String termo);

    @Query("CALL { " +
           "  MATCH (i:ImagemNode) WHERE i.textoExtraido IS NOT NULL AND toLower(i.textoExtraido) CONTAINS toLower($termo) RETURN i.documentoId AS docId " +
           "  UNION " +
           "  MATCH (e)-[:MENCIONADA_EM|LOCALIZADO_EM|OCORREU_EM|PARTICIPA_DE|TRATA_DE]->(i:ImagemNode) " +
           "  WHERE toLower(e.nome) CONTAINS toLower($termo) RETURN i.documentoId AS docId " +
           "} RETURN DISTINCT docId")
    List<Long> findDocumentIdsByImagemEntidadeNome(String termo);
}
