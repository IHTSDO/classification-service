package org.snomed.otf.reasoner.server;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.reasoner.server.ontology.DelegateOntology;
import org.snomed.otf.reasoner.server.ontology.OntologyService;
import org.snomed.otf.reasoner.server.taxonomy.ReasonerTaxonomyBuilder;
import org.snomed.otf.reasoner.server.taxonomy.Taxonomy;

import java.util.Date;

public class SnomedReasonerService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void classify(String releaseDirectoryPath, String reasonerFactoryClassName) throws ReleaseImportException, OWLOntologyCreationException {
		long start = new Date().getTime();
		logger.info("Building taxonomy");
		ReasonerTaxonomyBuilder reasonerTaxonomyBuilder = new ReasonerTaxonomyBuilder();
		Taxonomy taxonomy = reasonerTaxonomyBuilder.build(releaseDirectoryPath);

		logger.info("Creating OwlOntology");
		DelegateOntology delegateOntology = new OntologyService().createOntology();
		delegateOntology.setTaxonomy(taxonomy);

		logger.info("Creating OwlReasoner");
		final OWLReasonerConfiguration configuration = new SimpleConfiguration(new ConsoleProgressMonitor());
		OWLReasonerFactory reasonerFactory = getOWLReasonerFactory(reasonerFactoryClassName);
		OWLReasoner reasoner = reasonerFactory.createReasoner(delegateOntology, configuration);

		logger.info("OwlReasoner inferring class hierarchy");
		reasoner.flush();
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		logger.info("Inference complete");
		logger.info("Total {} seconds", (new Date().getTime() - start)/1000f);
	}

	private OWLReasonerFactory getOWLReasonerFactory(String reasonerFactoryClassName) throws ReasonerServiceRuntimeException {
		Class<?> reasonerFactoryClass = null;
		try {
			reasonerFactoryClass = Class.forName(reasonerFactoryClassName);
			return (OWLReasonerFactory) reasonerFactoryClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new ReasonerServiceRuntimeException(String.format("Requested reasoner class '%s' not found.", reasonerFactoryClassName), e);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ReasonerServiceRuntimeException(String.format("An instance of requested reasoner '%s' could not be created.", reasonerFactoryClass), e);
		}
	}

}
