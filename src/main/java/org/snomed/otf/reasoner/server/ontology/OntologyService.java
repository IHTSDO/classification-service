package org.snomed.otf.reasoner.server.ontology;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OntologyService {

	public static final String SNOMED_IRI = "http://snomed.info/id/";

	private final OWLOntologyManager manager;

	public OntologyService() {
		manager = OWLManager.createOWLOntologyManager();
		manager.addOntologyFactory(new DelegateOntologyFactory());
	}

	public DelegateOntology createOntology() throws OWLOntologyCreationException {
		IRI ontologyIRI = IRI.create(SNOMED_IRI);
		DelegateOntology delegateOntology = (DelegateOntology) manager.createOntology(ontologyIRI);
		return delegateOntology;
	}
}
