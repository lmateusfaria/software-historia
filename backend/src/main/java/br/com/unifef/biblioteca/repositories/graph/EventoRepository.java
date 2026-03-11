package br.com.unifef.biblioteca.repositories.graph;

import br.com.unifef.biblioteca.domains.graph.Evento;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoRepository extends Neo4jRepository<Evento, String> {
}
