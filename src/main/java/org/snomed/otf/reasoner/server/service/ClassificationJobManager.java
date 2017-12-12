package org.snomed.otf.reasoner.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.reasoner.server.pojo.Classification;
import org.snomed.otf.reasoner.server.pojo.ClassificationStatus;
import org.snomed.otf.owltoolkit.service.ReasonerServiceException;
import org.snomed.otf.owltoolkit.service.SnomedReasonerService;
import org.snomed.otf.reasoner.server.service.store.FileStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class ClassificationJobManager {

	private final SnomedReasonerService snomedReasonerService;

	private final FileStoreService fileStoreService;

	private final LinkedBlockingQueue<Classification> classificationQueue;

	private final Map<String, Classification> classificationMap;

	@Value("${classification.debug.ontology-dump}")
	private boolean outputOntologyFileForDebug;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public ClassificationJobManager(@Autowired FileStoreService fileStoreService,
									@Autowired SnomedReasonerService snomedReasonerService) {
		this.snomedReasonerService = snomedReasonerService;
		this.fileStoreService = fileStoreService;
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

			File resultsFile = fileStoreService.getResultsFile(classification);

			try (OutputStream resultsOutputStream = new FileOutputStream(resultsFile)) {
				snomedReasonerService.classify(
						classification.getClassificationId(),
						previousReleaseRf2SnapshotArchive,
						currentReleaseRf2DeltaArchive,
						resultsOutputStream,
						classification.getReasonerId(),
						outputOntologyFileForDebug);
			}
			classification.setStatus(ClassificationStatus.COMPLETED);
			logger.info("Classification complete. Results written to {}", resultsFile.getAbsolutePath());

		} catch (ReasonerServiceException e) {
			classificationFailed(classification, e, e.getMessage());
		} catch (IOException e) {
			classificationFailed(classification, e, "Failed to read or files RF2 files.");
		}
	}

	private void classificationFailed(Classification classification, Exception e, String message) {
		logger.error(message, e);
		classification.setStatus(ClassificationStatus.FAILED);
		classification.setErrorMessage(message);
		classification.setDeveloperMessage(e.getMessage());
	}

	public Classification getClassification(String classificationId) {
		return classificationMap.get(classificationId);
	}

	public InputStream getClassificationResults(Classification classification) throws FileNotFoundException {
		return fileStoreService.loadResults(classification);
	}
}
