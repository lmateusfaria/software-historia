package br.com.unifef.biblioteca.repositories.graph;

import br.com.unifef.biblioteca.domains.graph.ImagemNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagemNodeRepository extends Neo4jRepository<ImagemNode, Long> {
    List<ImagemNode> findByDocumentoId(Long documentoId);
}
