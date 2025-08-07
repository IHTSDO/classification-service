package org.snomed.otf.reasoner.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.io.IOUtils;
import org.ihtsdo.otf.jms.MessagingHelper;
import org.ihtsdo.otf.resourcemanager.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.owltoolkit.service.ReasonerServiceException;
import org.snomed.otf.owltoolkit.service.SnomedReasonerService;
import org.snomed.otf.owltoolkit.util.InputStreamSet;
import org.snomed.otf.reasoner.server.configuration.ClassificationJobResourceConfiguration;
import org.snomed.otf.reasoner.server.pojo.Classification;
import org.snomed.otf.reasoner.server.pojo.ClassificationStatus;
import org.snomed.otf.reasoner.server.pojo.ClassificationStatusAndMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Service
public class ClassificationJobManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationJobManager.class);

	private final ExecutorService executorService;
	private final SnomedReasonerService snomedReasonerService;
	private final ResourceManager classificationJobResourceManager;
	private final MessagingHelper messagingHelper;
	private final ObjectMapper objectMapper;
	private final DependencyService dependencyService;

	@Value("${classification.debug.ontology-dump}")
	private boolean outputOntologyFileForDebug;

	@Value("${classification.jms.job.queue}")
	private String classificationJobQueue;

	@Value("${classification.jms.status.time-to-live-seconds}")
	private int messageTimeToLiveSeconds;

	public ClassificationJobManager(ClassificationJobResourceConfiguration classificationJobResourceConfiguration,
									ResourceLoader cloudResourceLoader,
									SnomedReasonerService snomedReasonerService,
									MessagingHelper messagingHelper,
									ObjectMapper objectMapper,
									ExecutorService executorService,
									DependencyService dependencyService
	) {
		this.dependencyService = dependencyService;
		this.classificationJobResourceManager = new ResourceManager(classificationJobResourceConfiguration, cloudResourceLoader);
		this.snomedReasonerService = snomedReasonerService;
		this.messagingHelper = messagingHelper;
		this.objectMapper = objectMapper;
		this.executorService = executorService;
	}

	public Classification queueClassification(String previousPackage, String dependencyPackage, InputStream snomedRf2DeltaInputArchive,
											  String reasonerId, String responseMessageQueue, String branch) throws IOException {

		// Create classification configuration
		Classification classification = new Classification(previousPackage, dependencyPackage, branch, reasonerId);

		// Persist input archive
		try {
			classificationJobResourceManager.writeResource(ResourcePathHelper.getInputDeltaPath(classification), snomedRf2DeltaInputArchive);
			saveClassification(classification);
		} catch (IOException e) {
			throw new IOException("Failed to persist input archive.", e);
		}

		// Add to JMS message queue
		try {
			ActiveMQQueue responseDestination = responseMessageQueue == null ? null : new ActiveMQQueue(responseMessageQueue);
			messagingHelper.send(new ActiveMQQueue(classificationJobQueue), classification, null, responseDestination, messageTimeToLiveSeconds);
		} catch (JMSException e) {
			throw new IOException("Failed to add classification job to the message queue.", e);
		}

		return classification;
	}

	private void saveClassification(Classification classification) {
		try {
			String classificationString = objectMapper.writeValueAsString(classification);
			classificationJobResourceManager.writeResource(
					ResourcePathHelper.getClassificationPathFromToday(classification.getClassificationId()),
					IOUtils.toInputStream(classificationString, StandardCharsets.UTF_8));
		} catch (IOException e) {
			LOGGER.error("Failed to save classification {}", classification.getClassificationId(), e);
		}
	}

	@JmsListener(destination = "${classification.jms.job.queue}")
	public void consumeClassificationJob(TextMessage classificationMessage) throws JMSException, IOException {
		Classification classification = objectMapper.readValue(classificationMessage.getText(), Classification.class);
		Destination jmsReplyTo = classificationMessage.getJMSReplyTo();
		classify(classification, statusAndMessage -> {
			// Update classification in resource store
			classification.setStatus(statusAndMessage.getStatus());
			classification.setStatusMessage(statusAndMessage.getStatusMessage());
			saveClassification(classification);

			// Send notification via JMS
			if (jmsReplyTo != null) {
				sendStatusAsync(jmsReplyTo, statusAndMessage);
			}
		});
	}

	private void sendStatusAsync(Destination jmsReplyTo, ClassificationStatusAndMessage statusAndMessage) {
		executorService.submit(() -> {
			// Send notification via JMS
			try {
				messagingHelper.send(jmsReplyTo, statusAndMessage);
			} catch (JsonProcessingException | JMSException e) {
				LOGGER.error("Failed to send status update {} to {}", statusAndMessage, jmsReplyTo);
			}
		});
	}

	private void classify(Classification classification, Consumer<ClassificationStatusAndMessage> statusConsumer) {
		statusConsumer.accept(new ClassificationStatusAndMessage(ClassificationStatus.RUNNING, null, classification.getClassificationId()));
		LOGGER.info("Running classification {}, branch {}", classification.getClassificationId(), classification.getBranch());

		Set<String> previousPackages = new HashSet<>();
		if (classification.getPreviousPackage() != null) {
			previousPackages.add(classification.getPreviousPackage());
		}

		if (classification.getDependencyPackage() != null) {
			previousPackages.add(classification.getDependencyPackage());
		}

		File tempDeltaFile = null;
		InputStream originalDeltaArchive = null;
		ByteArrayInputStream toProcessMDRS = null;
		ByteArrayInputStream toCreateTempFile = null;
		try {
			// Delta
			originalDeltaArchive = classificationJobResourceManager.readResourceStream(ResourcePathHelper.getInputDeltaPath(classification));
			byte[] deltaArchiveBytes = StreamUtils.copyToByteArray(originalDeltaArchive);
			toProcessMDRS = new ByteArrayInputStream(deltaArchiveBytes);
			toCreateTempFile = new ByteArrayInputStream(deltaArchiveBytes);

			// Previous Snapshot + dependency
			InputStreamSet previousReleaseRf2SnapshotArchives = dependencyService.getInputStreamSet(previousPackages, toProcessMDRS);
			if (previousReleaseRf2SnapshotArchives == null) {
				throw new ReasonerServiceException("Dependencies not found from MDRS.");
			}

			tempDeltaFile = Files.createTempFile("classification-delta-" + classification.getClassificationId(), ".zip").toFile();
			StreamUtils.copy(toCreateTempFile, new FileOutputStream(tempDeltaFile));

			String resultsPath = ResourcePathHelper.getResultsPath(classification);
			try (OutputStream resultsOutputStream = classificationJobResourceManager.openWritableResourceStream(resultsPath);
				 InputStream deltaInputStream = new FileInputStream(tempDeltaFile)) {
				snomedReasonerService.classify(
						classification.getClassificationId(),
						previousReleaseRf2SnapshotArchives,
						deltaInputStream,
						resultsOutputStream,
						classification.getReasonerId(),
						outputOntologyFileForDebug);
			}
			statusConsumer.accept(new ClassificationStatusAndMessage(ClassificationStatus.COMPLETED, null, classification.getClassificationId()));
			LOGGER.info("Classification complete {}, branch {}. Results written to {}", classification.getClassificationId(), classification.getBranch(), resultsPath);
		} catch (Exception e) {
			LOGGER.error("Classification failed {}, branch {}. ", classification.getClassificationId(), classification.getBranch(), e);
			statusConsumer.accept(new ClassificationStatusAndMessage(ClassificationStatus.FAILED, e.getMessage(), classification.getClassificationId()));
		} finally {
			if (tempDeltaFile != null) {
				if (!tempDeltaFile.delete()) {
					LOGGER.warn("Failed to delete temp file {}", tempDeltaFile.getAbsolutePath());
				}
			}

			close(originalDeltaArchive);
			close(toProcessMDRS);
			close(toCreateTempFile);
		}
	}

	private void close(InputStream inputStream) {
		if (inputStream == null) {
			return;
		}

		try {
			inputStream.close();
		} catch (Exception e) {
			LOGGER.error("Failed to close InputStream", e);
		}
	}

	public Classification getClassification(String classificationId) throws FileNotFoundException {
		// Classifications are stored by date. We will guess the date and return nothing if the classification was more than 5 days ago.
		// This is to avoid loading a large number of classifications from the store.
		for (int i = 0; i < 5; i++) {
			String path = ResourcePathHelper.getClassificationPathFromPast(classificationId, i);
			try {
				InputStream inputStream = classificationJobResourceManager.readResourceStream(path);
				return objectMapper.readValue(inputStream, Classification.class);
			} catch (FileNotFoundException e) {
				// Try the next day
			} catch (IOException e) {
				LOGGER.error("Failed to load classification {} from {}", classificationId, path, e);
			}
		}
		throw new FileNotFoundException("Classification " + classificationId + " not found.");
	}

	public InputStream getClassificationResults(Classification classification) throws IOException {
		return classificationJobResourceManager.readResourceStream(ResourcePathHelper.getResultsPath(classification));
	}
}
