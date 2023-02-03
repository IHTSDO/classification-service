package org.snomed.otf.reasoner.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.snomed.otf.owltoolkit.service.SnomedReasonerService;
import org.snomed.otf.reasoner.server.configuration.TestConfiguration;
import org.snomed.otf.reasoner.server.pojo.Classification;
import org.snomed.otf.reasoner.server.pojo.ClassificationStatusAndMessage;
import org.snomed.otf.snomedboot.testutil.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.fail;
import static org.snomed.otf.reasoner.server.pojo.ClassificationStatus.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {TestConfiguration.class})
@ActiveProfiles("test")
public class ClassificationJobManagerIntegrationTest {

	@Autowired
	private ClassificationJobManager classificationJobManager;

	@Autowired
	private ObjectMapper objectMapper;

	private String baseReleasePath;
	private File newContentDeltaArchive;

	@BeforeEach
	public void setup() throws IOException {
		String baseReleaseRF2Path = "src/test/resources/SnomedCT_MiniRF2_Base_snapshot";
		String deltaRF2Path = "src/test/resources/SnomedCT_MiniRF2_Add_Diabetes_delta";

		// Zip base
		File previousReleaseSnapshotArchive = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines(baseReleaseRF2Path);
		previousReleaseSnapshotArchive.deleteOnExit();

		// Copy base into releases directory
		this.baseReleasePath = "base-snapshot.zip";
		File baseSnapshot = new File("store/releases/" + this.baseReleasePath);
		FileUtils.copyFile(previousReleaseSnapshotArchive, baseSnapshot);
		baseSnapshot.deleteOnExit();

		// Zip new content
		newContentDeltaArchive = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines(deltaRF2Path);
		newContentDeltaArchive.deleteOnExit();
	}

	@Test
	public void queueClassification() throws Exception {
		Classification classification = classificationJobManager.queueClassification(
				baseReleasePath,
				null,
				new FileInputStream(newContentDeltaArchive),
				SnomedReasonerService.ELK_REASONER_FACTORY,
				null, "MAIN"
		);

		GregorianCalendar timeout = new GregorianCalendar();
		timeout.add(Calendar.MINUTE, 2);

		while (classification.getStatus() == SCHEDULED || classification.getStatus() == RUNNING) {
			if (new Date().after(timeout.getTime())) {
				fail("Classification must complete before test timeout.");
			}
			Thread.sleep(1_000);
			classification = classificationJobManager.getClassification(classification.getClassificationId());
		}

		assertEquals("Classification status is COMPLETED", COMPLETED, classification.getStatus());
	}

	private ClassificationStatusAndMessage classificationStatus;

	@JmsListener(destination = "test.job.status.topic", containerFactory = "topicJmsListenerContainerFactory")
	public void readJobStatusUpdate(String content) throws IOException {
		ClassificationStatusAndMessage classificationStatus = objectMapper.readValue(content, ClassificationStatusAndMessage.class);
		this.classificationStatus = classificationStatus;
	}

	@Test
	public void queueClassificationReadStatusViaJMS() throws Exception {

		String responseMessageTopic = "test.job.status.topic";

		Classification classification = classificationJobManager.queueClassification(
				baseReleasePath,
				null,
				new FileInputStream(newContentDeltaArchive),
				SnomedReasonerService.ELK_REASONER_FACTORY,
				responseMessageTopic,
				"MAIN");

		GregorianCalendar timeout = new GregorianCalendar();
		timeout.add(Calendar.MINUTE, 1);

		while (classificationStatus == null || classificationStatus.getStatus() == SCHEDULED || classificationStatus.getStatus() == RUNNING) {
			if (new Date().after(timeout.getTime())) {
				fail("Classification must complete before test timeout.");
			}
			Thread.sleep(1_000);
			// No need to read anything here, status comes in via JMS in readJobStatusUpdate method
		}

		assertEquals("Classification ID's equal", classification.getClassificationId(), classificationStatus.getId());
		assertEquals("Classification status is COMPLETED", COMPLETED, classificationStatus.getStatus());
	}

}
