package org.snomed.otf.reasoner.server.service;

import com.amazonaws.util.StringInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.command.ActiveMQQueue;
import org.ihtsdo.otf.jms.MessagingHelper;
import org.ihtsdo.otf.resourcemanager.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.owltoolkit.service.ReasonerServiceException;
import org.snomed.otf.owltoolkit.service.SnomedReasonerService;
import org.snomed.otf.owltoolkit.util.InputStreamSet;
import org.snomed.otf.reasoner.server.configuration.ClassificationJobResourceConfiguration;
import org.snomed.otf.reasoner.server.configuration.SnomedReleaseResourceConfiguration;
import org.snomed.otf.reasoner.server.pojo.Classification;
import org.snomed.otf.reasoner.server.pojo.ClassificationStatus;
import org.snomed.otf.reasoner.server.pojo.ClassificationStatusAndMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Service
public class ClassificationJobManager {

	private final SnomedReasonerService snomedReasonerService;

	private final ResourceManager snomedReleaseResourceManager;

	private final ResourceManager classificationJobResourceManager;

	private final MessagingHelper messagingHelper;

	private final ObjectMapper objectMapper;

	@Value("${classification.debug.ontology-dump}")
	private boolean outputOntologyFileForDebug;

	@Value("${classification.jms.job.queue}")
	private String classificationJobQueue;

	@Value("${classification.jms.status.time-to-live-seconds}")
	private int messageTimeToLiveSeconds;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public ClassificationJobManager(
			@Autowired SnomedReleaseResourceConfiguration snomedReleaseResourceConfiguration,
			@Autowired ClassificationJobResourceConfiguration classificationJobResourceConfiguration,
			@Autowired ResourceLoader cloudResourceLoader,
			@Autowired SnomedReasonerService snomedReasonerService,
			@Autowired MessagingHelper messagingHelper,
			@Autowired ObjectMapper objectMapper) {

		snomedReleaseResourceManager = new ResourceManager(snomedReleaseResourceConfiguration, cloudResourceLoader);
		classificationJobResourceManager = new ResourceManager(classificationJobResourceConfiguration, cloudResourceLoader);
		this.snomedReasonerService = snomedReasonerService;
		this.messagingHelper = messagingHelper;
		this.objectMapper = objectMapper;
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
					new StringInputStream(classificationString));
		} catch (IOException e) {
			logger.error("Failed to save classification {}", classification.getClassificationId(), e);
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
				try {
					messagingHelper.send(jmsReplyTo, statusAndMessage);
				} catch (JsonProcessingException | JMSException e) {
					logger.error("Failed to send status update {} to {}", statusAndMessage, jmsReplyTo);
				}
			}
		});
	}

	private void classify(Classification classification, Consumer<ClassificationStatusAndMessage> statusConsumer) {
		statusConsumer.accept(new ClassificationStatusAndMessage(ClassificationStatus.RUNNING));
		logger.info("Running classification {}, branch {}", classification.getClassificationId(), classification.getBranch());

		Set<String> previousPackages = new HashSet<>();
		if (classification.getPreviousPackage() != null) {
			previousPackages.add(classification.getPreviousPackage());
		}
		if (classification.getDependencyPackage() != null) {
			previousPackages.add(classification.getDependencyPackage());
		}

		try (InputStreamSet previousReleaseRf2SnapshotArchives = getInputStreams(previousPackages);
			 InputStream currentReleaseRf2DeltaArchive = classificationJobResourceManager.readResourceStream(ResourcePathHelper.getInputDeltaPath(classification))) {

			String resultsPath = ResourcePathHelper.getResultsPath(classification);
			try (OutputStream resultsOutputStream = classificationJobResourceManager.writeResourceStream(resultsPath)) {
				snomedReasonerService.classify(
						classification.getClassificationId(),
						previousReleaseRf2SnapshotArchives,
						currentReleaseRf2DeltaArchive,
						resultsOutputStream,
						classification.getReasonerId(),
						outputOntologyFileForDebug);
			}
			statusConsumer.accept(new ClassificationStatusAndMessage(ClassificationStatus.COMPLETED));
			logger.info("Classification complete {}, branch {}. Results written to {}", classification.getClassificationId(), classification.getBranch(), resultsPath);

		} catch (ReasonerServiceException | IOException e) {
			logger.error("Classification failed {}, branch {}. ", e.getMessage(), classification.getClassificationId(), classification.getBranch(), e);
			statusConsumer.accept(new ClassificationStatusAndMessage(ClassificationStatus.FAILED, e.getMessage()));
		}
	}

	/**
	 * Load all the previous release archives from the release resource manager as streams.
	 * @param previousReleases relative path of the archives to be loaded.
	 * @return InputStreamSet
	 */
	private InputStreamSet getInputStreams(Set<String> previousReleases) throws IOException {
		Set<InputStream> previousReleaseStreams = new HashSet<>();
		try {
			for (String previousRelease : previousReleases) {
				previousReleaseStreams.add(snomedReleaseResourceManager.readResourceStream(previousRelease));
			}
		} catch (IOException e) {
			previousReleaseStreams.forEach(inputStream -> {
				try {
					inputStream.close();
				} catch (IOException closeException) {
					logger.error("Failed to close stream.", closeException);
				}
			});
			throw e;
		}

		return new InputStreamSet(previousReleaseStreams.toArray(new InputStream[]{}));
	}

	public Classification getClassification(String classificationId) throws FileNotFoundException {
		// Classifications are stored by date. We will guess the date and return nothing if the classification was more than 5 days ago.
		for (int daysInPast = 0; daysInPast <= 5; daysInPast++) {
			String classificationPath = ResourcePathHelper.getClassificationPathFromPast(classificationId, daysInPast);
			try {
				InputStream inputStream = classificationJobResourceManager.readResourceStreamOrNullIfNotExists(classificationPath);
				if (inputStream != null) {
					try {
						return objectMapper.readValue(inputStream, Classification.class);
					} finally {
						inputStream.close();
					}
				}
			} catch (IOException e) {
				logger.error("Failed to load classification from store.", e);
			}
		}
		throw new FileNotFoundException("Classification with ID " + classificationId + " not found.");
	}

	public InputStream getClassificationResults(Classification classification) throws IOException {
		return classificationJobResourceManager.readResourceStream(ResourcePathHelper.getResultsPath(classification));
	}

}
