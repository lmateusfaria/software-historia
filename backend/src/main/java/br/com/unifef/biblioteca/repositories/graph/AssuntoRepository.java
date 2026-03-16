package br.com.unifef.biblioteca.repositories.graph;

import br.com.unifef.biblioteca.domains.graph.Assunto;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssuntoRepository extends Neo4jRepository<Assunto, String> {
    Optional<Assunto> findByNome(String nome);
}
