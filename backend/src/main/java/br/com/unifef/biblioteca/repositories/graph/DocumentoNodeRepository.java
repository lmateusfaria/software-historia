package br.com.unifef.biblioteca.repositories.graph;

import br.com.unifef.biblioteca.domains.graph.DocumentoNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoNodeRepository extends Neo4jRepository<DocumentoNode, Long> {
}
