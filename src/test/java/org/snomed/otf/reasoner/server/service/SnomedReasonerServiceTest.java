package org.snomed.otf.reasoner.server.service;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.snomed.otf.reasoner.server.Configuration;
import org.snomed.otf.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(Configuration.class)
public class SnomedReasonerServiceTest {

	@Autowired
	private SnomedReasonerService snomedReasonerService;

	private static final String REASONER_FACTORY_CLASS_NAME = "org.semanticweb.elk.owlapi.ElkReasonerFactory";

	@Test
	public void testClassify() throws IOException, OWLOntologyCreationException, ReleaseImportException, ReasonerServiceException {
		File baseRF2SnapshotZip = ZipUtil.zipDirectory("src/test/resources/SnomedCT_MiniRF2_Base_snapshot");
		File emptyDeltaZip = ZipUtil.zipDirectory("src/test/resources/SnomedCT_MiniRF2_Empty_delta");
//		File emptyDeltaZip = ZipUtil.zipDirectory("src/test/resources/SnomedCT_MiniRF2_Add_Diabetes_delta");
		assertNotNull(snomedReasonerService);
		snomedReasonerService.classify(new FileInputStream(baseRF2SnapshotZip), new FileInputStream(emptyDeltaZip), REASONER_FACTORY_CLASS_NAME);
	}

}
