package br.com.unifef.biblioteca.repositories.graph;

import br.com.unifef.biblioteca.domains.graph.Pessoa;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PessoaRepository extends Neo4jRepository<Pessoa, String> {
}
