package org.snomed.otf.reasoner.server.service.ontology;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.EmptyInMemOWLOntologyFactory;

public class DelegateOntologyFactory extends EmptyInMemOWLOntologyFactory implements OWLOntologyFactory {

	@Override
	public boolean canCreateFromDocumentIRI(IRI documentIRI) {
		return documentIRI.toString().startsWith(OntologyService.SNOMED_IRI);
	}

	@Override
	public OWLOntology createOWLOntology(OWLOntologyID ontologyID, IRI documentIRI, OWLOntologyCreationHandler handler) throws OWLOntologyCreationException {
		DelegateOntology delegateOntology = new DelegateOntology(getOWLOntologyManager(), ontologyID);
		handler.ontologyCreated(delegateOntology);
		return delegateOntology;
	}
}
