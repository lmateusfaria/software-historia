package br.com.unifef.biblioteca.Main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;


@ComponentScan(basePackages = "br.com.unifef.biblioteca")
@EntityScan(basePackages = {
    "br.com.unifef.biblioteca.domains",
    "br.com.unifef.biblioteca.domains.enums",
    "br.com.unifef.biblioteca.domains.graph"
})
@EnableJpaRepositories(basePackages = "br.com.unifef.biblioteca.repositories")
@EnableNeo4jRepositories(basePackages = "br.com.unifef.biblioteca.repositories.graph")
@EnableScheduling
@SpringBootApplication
public class MainApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}


}
