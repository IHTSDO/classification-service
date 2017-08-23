package org.snomed.otf.reasoner.server.service;

import org.coode.owlapi.functionalrenderer.OWLFunctionalSyntaxRenderer;
import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.reasoner.server.pojo.Classification;
import org.snomed.otf.reasoner.server.pojo.ClassificationStatus;
import org.snomed.otf.reasoner.server.service.classification.ReasonerTaxonomy;
import org.snomed.otf.reasoner.server.service.classification.ReasonerTaxonomyWalker;
import org.snomed.otf.reasoner.server.service.normalform.RelationshipChangeCollector;
import org.snomed.otf.reasoner.server.service.normalform.RelationshipNormalFormGenerator;
import org.snomed.otf.reasoner.server.service.ontology.DelegateOntology;
import org.snomed.otf.reasoner.server.service.ontology.OntologyService;
import org.snomed.otf.reasoner.server.service.store.FileStoreService;
import org.snomed.otf.reasoner.server.service.taxonomy.ExistingTaxonomy;
import org.snomed.otf.reasoner.server.service.taxonomy.ExistingTaxonomyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class SnomedReasonerService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private final FileStoreService fileStoreService;

	private final LinkedBlockingQueue<Classification> classificationQueue;

	private final Map<String, Classification> classificationMap;

	private final ClassificationResultsService resultsService;

	public SnomedReasonerService(@Autowired FileStoreService fileStoreService, @Autowired ClassificationResultsService resultsService) {
		this.fileStoreService = fileStoreService;
		this.resultsService = resultsService;
		classificationQueue = new LinkedBlockingQueue<>();
		classificationMap = new HashMap<>();
	}

	@PostConstruct
	public void init() {
		new Thread(() -> {
			try {
				Classification classification;
				while (true) {
					if ((classification = classificationQueue.poll(1, TimeUnit.SECONDS)) != null) {
						classify(classification);
					}
				}
			} catch (InterruptedException e) {
				// Nothing wrong
			}
			logger.info("Shutting down.");
		}, "job-polling-thread").start();
	}

	public Classification queueClassification(String previousRelease, InputStream snomedRf2SnapshotArchive, String reasonerId, String branch) throws IOException {
		// Create classification configuration
		Classification classification = new Classification(previousRelease, branch, reasonerId);

		// Persist input archive
		try {
			fileStoreService.saveDeltaInput(classification, snomedRf2SnapshotArchive);
		} catch (IOException e) {
			throw new IOException("Failed to persist input archive.", e);
		}

		// Add to queue
		classificationMap.put(classification.getClassificationId(), classification);
		classificationQueue.add(classification);

		return classification;
	}

	private void classify(Classification classification) {
		classification.setStatus(ClassificationStatus.RUNNING);
		try (InputStream previousReleaseRf2SnapshotArchive = fileStoreService.loadPreviousRelease(classification.getPreviousRelease());
			 InputStream currentReleaseRf2DeltaArchive = fileStoreService.loadDeltaInput(classification)) {

			File resultsArchive = classify(previousReleaseRf2SnapshotArchive, currentReleaseRf2DeltaArchive, classification.getReasonerId());

			fileStoreService.saveResults(classification, resultsArchive);
			classification.setStatus(ClassificationStatus.COMPLETED);
		} catch (ReasonerServiceException e) {
			classificationFailed(classification, e, e.getMessage());
		} catch (OWLOntologyCreationException e) {
			classificationFailed(classification, e, "Failed to create OWL Ontology.");
		} catch (ReleaseImportException e) {
			classificationFailed(classification, e, "Failed to import RF2 content.");
		} catch (IOException e) {
			classificationFailed(classification, e, "Failed to load RF2 files.");
		}
	}

	private void classificationFailed(Classification classification, Exception e, String message) {
		logger.error(message, e);
		classification.setStatus(ClassificationStatus.FAILED);
		classification.setErrorMessage(message);
		classification.setDeveloperMessage(e.getMessage());
	}

	public File classify(InputStream previousReleaseRf2SnapshotArchive, InputStream currentReleaseRf2DeltaArchive, String reasonerFactoryClassName) throws ReleaseImportException, OWLOntologyCreationException, ReasonerServiceException {
		Date startDate = new Date();
		logger.info("Checking requested reasoner is available");
		OWLReasonerFactory reasonerFactory = getOWLReasonerFactory(reasonerFactoryClassName);

		logger.info("Building existingTaxonomy");
		ExistingTaxonomyBuilder existingTaxonomyBuilder = new ExistingTaxonomyBuilder();
		ExistingTaxonomy existingTaxonomy = existingTaxonomyBuilder.build(previousReleaseRf2SnapshotArchive, currentReleaseRf2DeltaArchive);

		logger.info("Creating OwlOntology");
		DelegateOntology delegateOntology = new OntologyService().createOntology();
		delegateOntology.setExistingTaxonomy(existingTaxonomy);

		// Uncomment for debugging
		// serialiseOntologyForDebug(delegateOntology);

		logger.info("Creating OwlReasoner");
		final OWLReasonerConfiguration configuration = new SimpleConfiguration(new ConsoleProgressMonitor());
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

	private OWLReasonerFactory getOWLReasonerFactory(String reasonerFactoryClassName) throws ReasonerServiceException {
		Class<?> reasonerFactoryClass = null;
		try {
			reasonerFactoryClass = Class.forName(reasonerFactoryClassName);
			return (OWLReasonerFactory) reasonerFactoryClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new ReasonerServiceException(String.format("Requested reasoner class '%s' not found.", reasonerFactoryClassName), e);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ReasonerServiceException(String.format("An instance of requested reasoner '%s' could not be created.", reasonerFactoryClass), e);
		}
	}

	private void serialiseOntologyForDebug(OWLOntology ontology) {
		OWLFunctionalSyntaxRenderer ontologyRenderer = new OWLFunctionalSyntaxRenderer();
		try {
			File classificationsDirectory = new File("debug/classificationMap");
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

	public Classification getClassification(String classificationId) {
		return classificationMap.get(classificationId);
	}

	public InputStream getClassificationResults(Classification classification) throws FileNotFoundException {
		return fileStoreService.loadResults(classification);
	}
}
