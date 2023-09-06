package org.snomed.otf.reasoner.server;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.snomed.otf.reasoner.server.configuration.Configuration;
import org.springframework.boot.SpringApplication;

public class Application extends Configuration {
    public static void main(String[] args) throws OWLOntologyCreationException {
        SpringApplication.run(Application.class, args);
    }
}
