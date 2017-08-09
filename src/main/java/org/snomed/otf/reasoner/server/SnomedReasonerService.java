package org.snomed.otf.reasoner.server;

import org.coode.owlapi.functionalrenderer.OWLFunctionalSyntaxRenderer;
import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.reasoner.server.classification.ReasonerTaxonomy;
import org.snomed.otf.reasoner.server.classification.ReasonerTaxonomyWalker;
import org.snomed.otf.reasoner.server.normalform.RelationshipChangeCollector;
import org.snomed.otf.reasoner.server.normalform.RelationshipNormalFormGenerator;
import org.snomed.otf.reasoner.server.ontology.DelegateOntology;
import org.snomed.otf.reasoner.server.ontology.OntologyService;
import org.snomed.otf.reasoner.server.taxonomy.ExistingTaxonomy;
import org.snomed.otf.reasoner.server.taxonomy.ExistingTaxonomyBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Service
public class SnomedReasonerService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void classify(InputStream snomedRf2SnapshotArchive, String reasonerFactoryClassName) throws ReleaseImportException, OWLOntologyCreationException {
		long start = new Date().getTime();
		logger.info("Building existingTaxonomy");
		ExistingTaxonomyBuilder existingTaxonomyBuilder = new ExistingTaxonomyBuilder();
		ExistingTaxonomy existingTaxonomy = existingTaxonomyBuilder.build(snomedRf2SnapshotArchive);

		logger.info("Creating OwlOntology");
		DelegateOntology delegateOntology = new OntologyService().createOntology();
		delegateOntology.setExistingTaxonomy(existingTaxonomy);

		logger.info("Creating OwlReasoner");
		final OWLReasonerConfiguration configuration = new SimpleConfiguration(new ConsoleProgressMonitor());
		OWLReasonerFactory reasonerFactory = getOWLReasonerFactory(reasonerFactoryClassName);
		OWLReasoner reasoner = reasonerFactory.createReasoner(delegateOntology, configuration);

		logger.info("OwlReasoner inferring class hierarchy");
		reasoner.flush();
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		logger.info("Inference complete");
		logger.info("{} seconds so far", (new Date().getTime() - start)/1000f);

		logger.info("Extract ReasonerTaxonomy");
		ReasonerTaxonomyWalker walker = new ReasonerTaxonomyWalker(reasoner, new ReasonerTaxonomy(), delegateOntology.getPrefixManager());
		ReasonerTaxonomy reasonerTaxonomy = walker.walk();

		logger.info("Generate normal form");
		RelationshipNormalFormGenerator normalFormGenerator = new RelationshipNormalFormGenerator(reasonerTaxonomy, existingTaxonomy);
		RelationshipChangeCollector changeCollector = new RelationshipChangeCollector();
		normalFormGenerator.collectNormalFormChanges(changeCollector);

		logger.info("{} relationships added, {} removed", changeCollector.getAddedCount(), changeCollector.getRemovedCount());

		logger.info("{} seconds total", (new Date().getTime() - start)/1000f);
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
