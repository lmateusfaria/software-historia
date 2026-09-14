package br.com.unifef.biblioteca;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Configuração mínima usada apenas pelos testes de fatia MVC ({@code @WebMvcTest}).
 * <p>
 * A aplicação real ({@code MainApplication}) fica no pacote {@code br.com.unifef.biblioteca.Main},
 * fora da árvore de pacotes-pai dos testes de controller (que residem em
 * {@code br.com.unifef.biblioteca.resources}), então o mecanismo de auto-detecção do
 * Spring Boot Test não a encontra. Esta classe fica no pacote raiz {@code br.com.unifef.biblioteca}
 * (ancestral de {@code .resources}) apenas para ser localizada por essa busca.
 * <p>
 * Deliberadamente NÃO possui {@code @ComponentScan}: cada classe de teste declara
 * explicitamente, via {@code @Import}, o(s) controller(s) sob teste (e {@link
 * br.com.unifef.biblioteca.resources.exceptions.ResourceExceptionHandler} quando relevante),
 * evitando arrastar outros controllers ou serviços/listeners que dependem de JPA, Neo4j e
 * MinIO — indisponíveis na fatia de teste.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
public class TestWebMvcSupport {
}
