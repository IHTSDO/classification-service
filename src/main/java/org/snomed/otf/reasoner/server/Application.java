package org.snomed.otf.reasoner.server;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.reasoner.server.configuration.Configuration;
import org.snomed.otf.reasoner.server.service.ClassificationJobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.not;
import static springfox.documentation.builders.PathSelectors.regex;

@EnableSwagger2
public class Application extends Configuration {

	@Autowired
	private ClassificationJobManager classificationJobManager;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) throws OWLOntologyCreationException {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(not(regex("/error")))
				.paths(not(regex("/")))
				.build();
	}

}
