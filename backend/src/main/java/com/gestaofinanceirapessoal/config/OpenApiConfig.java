package com.gestaofinanceirapessoal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do SpringDoc OpenAPI para documentação da API
 * do sistema de Gestão Financeira Pessoal.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestão Financeira Pessoal API")
                        .version("1.0.0")
                        .description("API REST para gerenciamento de finanças pessoais, incluindo controle de transações, categorias, cartões de crédito e pendências financeiras.")
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("dev@gestaofinanceira.com")));
    }
}

