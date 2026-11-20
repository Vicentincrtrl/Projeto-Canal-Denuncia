package com.example.projetofatec;
// Imports
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
// A classe ServletInitializer é responsável por inicializar a aplicação Spring Boot quando implantada em um servidor de servlet,
public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ProjetofatecApplication.class);
	}

}
