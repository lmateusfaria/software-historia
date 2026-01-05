package com.gestaofinanceirapessoal.Main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@ComponentScan(basePackages = "com.gestaofinanceirapessoal")
@EntityScan(basePackages = {"com.gestaofinanceirapessoal.domains","com.gestaofinanceirapessoal.domains.enums"})
@EnableJpaRepositories(basePackages = "com.gestaofinanceirapessoal.repositories")
@SpringBootApplication
public class MainApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}


}
