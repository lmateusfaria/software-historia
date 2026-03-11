package br.com.unifef.biblioteca.repositories.graph;

import br.com.unifef.biblioteca.domains.graph.Organizacao;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizacaoRepository extends Neo4jRepository<Organizacao, String> {
}
