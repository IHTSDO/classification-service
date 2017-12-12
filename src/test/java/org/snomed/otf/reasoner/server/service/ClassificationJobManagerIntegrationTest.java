package org.snomed.otf.reasoner.server.service;

import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.snomed.otf.owltoolkit.service.SnomedReasonerService;
import org.snomed.otf.owltoolkit.testutil.ZipUtil;
import org.snomed.otf.reasoner.server.Configuration;
import org.snomed.otf.reasoner.server.pojo.Classification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.snomed.otf.reasoner.server.pojo.ClassificationStatus.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(Configuration.class)
public class ClassificationJobManagerIntegrationTest {

	@Autowired
	private ClassificationJobManager classificationJobManager;
	private String baseReleasePath;
	private File newContentDeltaArchive;

	@Before
	public void setup() throws IOException {
		String baseReleaseRF2Path = "src/test/resources/SnomedCT_MiniRF2_Base_snapshot";
		String deltaRF2Path = "src/test/resources/SnomedCT_MiniRF2_Add_Diabetes_delta";

		// Zip base
		File previousReleaseSnapshotArchive = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines(baseReleaseRF2Path);
		previousReleaseSnapshotArchive.deleteOnExit();

		// Copy base into releases directory
		this.baseReleasePath = "base-snapshot.zip";
		File baseSnapshot = new File("store/releases/" + this.baseReleasePath);
		Files.copy(previousReleaseSnapshotArchive, baseSnapshot);
		baseSnapshot.deleteOnExit();

		// Zip new content
		newContentDeltaArchive = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines(deltaRF2Path);
		newContentDeltaArchive.deleteOnExit();
	}

	@Test
	public void queueClassification() throws Exception {
		Classification classification = classificationJobManager.queueClassification(
				baseReleasePath,
				new FileInputStream(newContentDeltaArchive),
				SnomedReasonerService.ELK_REASONER_FACTORY,
				"MAIN"
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

}
