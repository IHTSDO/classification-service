package org.snomed.otf.reasoner.server.service;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snomed.module.storage.ModuleMetadata;
import org.snomed.otf.owltoolkit.service.SnomedReasonerService;
import org.snomed.otf.reasoner.server.configuration.TestConfiguration;
import org.snomed.otf.reasoner.server.pojo.Classification;
import org.snomed.otf.reasoner.server.pojo.ClassificationStatusAndMessage;
import org.snomed.otf.snomedboot.testutil.ZipUtil;
import org.springframework.jms.annotation.JmsListener;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.snomed.otf.reasoner.server.pojo.ClassificationStatus.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

class ClassificationJobManagerIntegrationTest extends TestConfiguration {
	private String baseReleasePath;
	private File newContentDeltaArchive;
	private File baseSnapshot;

	@BeforeEach
	public void setup() throws IOException {
		String baseReleaseRF2Path = "src/test/resources/SnomedCT_MiniRF2_Base_snapshot";
		String deltaRF2Path = "src/test/resources/SnomedCT_MiniRF2_Add_Diabetes_delta";

		// Zip base
		File previousReleaseSnapshotArchive = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines(baseReleaseRF2Path);
		previousReleaseSnapshotArchive.deleteOnExit();

		// Copy base into releases directory
		this.baseReleasePath = "base-snapshot.zip";
		this.baseSnapshot = new File("store/releases/" + this.baseReleasePath);
		FileUtils.copyFile(previousReleaseSnapshotArchive, this.baseSnapshot);
		this.baseSnapshot.deleteOnExit();

		// Zip new content
		this.newContentDeltaArchive = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines(deltaRF2Path);
		this.newContentDeltaArchive.deleteOnExit();
	}

	@Test
	public void queueClassification() throws Exception {
		givenDependency("20170131");

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

	@JmsListener(destination = "test.job.status.queue")
	public void readJobStatusUpdate(ClassificationStatusAndMessage classificationStatus) {
		this.classificationStatus = classificationStatus;
	}

	@Test
	public void queueClassificationReadStatusViaJMS() throws Exception {
		givenDependency("20170131");

		String responseMessageQueue = "test.job.status.queue";

		Classification classification = classificationJobManager.queueClassification(
				baseReleasePath,
				null,
				new FileInputStream(newContentDeltaArchive),
				SnomedReasonerService.ELK_REASONER_FACTORY,
				responseMessageQueue,
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

	private void givenDependency(String effectiveTime) {
		ModuleMetadata moduleMetadata = new ModuleMetadata();
		moduleMetadata.setCodeSystemShortName("TEST");
		moduleMetadata.setIdentifyingModuleId("900000000000207008");
		moduleMetadata.setEffectiveTime(Integer.valueOf(effectiveTime));
		moduleMetadata.setFilename("SnomedCT_MiniRF2_Base_snapshot");
		moduleMetadata.setFile(this.baseSnapshot);

		when(moduleStorageCoordinator.getDependencies(any(), eq(true))).thenReturn(Set.of(moduleMetadata));
	}
}
