package br.com.unifef.biblioteca.repositories.graph;

import br.com.unifef.biblioteca.domains.graph.ImagemNode;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagemNodeRepository extends Neo4jRepository<ImagemNode, Long> {
    List<ImagemNode> findByDocumentoId(Long documentoId);

    @Query("MATCH (i:ImagemNode)<-[:MENCIONADA_EM|LOCALIZADO_EM|OCORREU_EM|PARTICIPA_DE|TRATA_DE]-(e) " +
           "WHERE toLower(e.nome) CONTAINS toLower($termo) OR (i.textoExtraido IS NOT NULL AND toLower(i.textoExtraido) CONTAINS toLower($termo)) " +
           "RETURN i")
    List<ImagemNode> findByEntidadeNomeOuTexto(String termo);
}
