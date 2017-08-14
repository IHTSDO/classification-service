package org.snomed.otf.reasoner.server.service;

import org.coode.owlapi.functionalrenderer.OWLFunctionalSyntaxRenderer;
import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.reasoner.server.service.classification.ReasonerTaxonomy;
import org.snomed.otf.reasoner.server.service.classification.ReasonerTaxonomyWalker;
import org.snomed.otf.reasoner.server.service.normalform.RelationshipChangeCollector;
import org.snomed.otf.reasoner.server.service.normalform.RelationshipNormalFormGenerator;
import org.snomed.otf.reasoner.server.service.ontology.DelegateOntology;
import org.snomed.otf.reasoner.server.service.ontology.OntologyService;
import org.snomed.otf.reasoner.server.service.taxonomy.ExistingTaxonomy;
import org.snomed.otf.reasoner.server.service.taxonomy.ExistingTaxonomyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Service
public class SnomedReasonerService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private ClassificationResultsService resultsService;

	public SnomedReasonerService(@Autowired ClassificationResultsService resultsService) {
		this.resultsService = resultsService;
	}

	public void queueClassification(InputStream snomedRf2SnapshotArchive, String reasonerFactoryClassName) {
		// Write archive and config to persistent storage
		// Map of jobs
		// Queue
		// Poll queue for jobs
		// Update job status when start
		// Classify
		// Write results to persistent storage
		// Update job status
	}

	public File classify(InputStream snomedRf2SnapshotArchive, String reasonerFactoryClassName) throws ReleaseImportException, OWLOntologyCreationException {
		Date startDate = new Date();
		logger.info("Building existingTaxonomy");
		ExistingTaxonomyBuilder existingTaxonomyBuilder = new ExistingTaxonomyBuilder();
		ExistingTaxonomy existingTaxonomy = existingTaxonomyBuilder.build(snomedRf2SnapshotArchive);

		logger.info("Creating OwlOntology");
		DelegateOntology delegateOntology = new OntologyService().createOntology();
		delegateOntology.setExistingTaxonomy(existingTaxonomy);

		// Uncomment for debugging
		// serialiseOntologyForDebug(delegateOntology);

		logger.info("Creating OwlReasoner");
		final OWLReasonerConfiguration configuration = new SimpleConfiguration(new ConsoleProgressMonitor());
		OWLReasonerFactory reasonerFactory = getOWLReasonerFactory(reasonerFactoryClassName);
		OWLReasoner reasoner = reasonerFactory.createReasoner(delegateOntology, configuration);

		logger.info("OwlReasoner inferring class hierarchy");
		reasoner.flush();
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		logger.info("Inference complete");
		logger.info("{} seconds so far", (new Date().getTime() - startDate.getTime())/1000f);

		logger.info("Extract ReasonerTaxonomy");
		ReasonerTaxonomyWalker walker = new ReasonerTaxonomyWalker(reasoner, new ReasonerTaxonomy(), delegateOntology.getPrefixManager());
		ReasonerTaxonomy reasonerTaxonomy = walker.walk();

		logger.info("Generate normal form");
		RelationshipNormalFormGenerator normalFormGenerator = new RelationshipNormalFormGenerator(reasonerTaxonomy, existingTaxonomy);
		RelationshipChangeCollector changeCollector = new RelationshipChangeCollector();
		normalFormGenerator.collectNormalFormChanges(changeCollector);

		logger.info("{} relationships added, {} removed", changeCollector.getAddedCount(), changeCollector.getRemovedCount());

		logger.info("Writing results archive");
		File resultsRf2Archive = resultsService.createResultsRf2Archive(changeCollector, reasonerTaxonomy.getEquivalentConceptIds(), startDate);

		logger.info("{} seconds total", (new Date().getTime() - startDate.getTime())/1000f);

		return resultsRf2Archive;
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

	private void serialiseOntologyForDebug(OWLOntology ontology) {
		OWLFunctionalSyntaxRenderer ontologyRenderer = new OWLFunctionalSyntaxRenderer();
		try {
			File classificationsDirectory = new File("debug/classifications");
			classificationsDirectory.mkdirs();
			File owlFile = new File(classificationsDirectory, new Date().getTime() + ".owl");
			logger.info("Serialising OWL Ontology before classification to file {}", owlFile.getAbsolutePath());
			try (FileWriter fileWriter = new FileWriter(owlFile)) {
				ontologyRenderer.render(ontology, fileWriter);
			}
		} catch (OWLRendererException | IOException e) {
			logger.error("Failed to serialise OWL Ontology.", e);
		}
	}

}
