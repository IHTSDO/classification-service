package org.snomed.otf.reasoner.server.service;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.snomed.otf.reasoner.server.Application;
import org.snomed.otf.reasoner.server.Configuration;
import org.snomed.otf.reasoner.server.service.constants.Concepts;
import org.snomed.otf.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.snomed.otf.reasoner.server.service.TestFileUtil.readInferredRelationshipLinesTrim;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(Configuration.class)
public class SimpleClassificationIntegrationTest {

	@Autowired
	private SnomedReasonerService snomedReasonerService;

	private static final String FINDING_SITE = "363698007";

	@Test
	public void testClassifyBase() throws IOException, OWLOntologyCreationException, ReleaseImportException, ReasonerServiceException {
		File baseRF2SnapshotZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Base_snapshot");
		File emptyDeltaZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Empty_delta");
		assertNotNull(snomedReasonerService);

		// Run classification
		File results = snomedReasonerService.classify("", new FileInputStream(baseRF2SnapshotZip), new FileInputStream(emptyDeltaZip), Application.DEFAULT_REASONER_FACTORY);

		// Assert results
		List<String> lines = readInferredRelationshipLinesTrim(results);
		assertEquals("Relationship delta should only contain the header line.",1, lines.size());
	}

	@Test
	public void testClassifyNewConcept() throws IOException, OWLOntologyCreationException, ReleaseImportException, ReasonerServiceException {
		File baseRF2SnapshotZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Base_snapshot");
		File newDiabetesConceptDeltaZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Add_Diabetes_delta");
		assertNotNull(snomedReasonerService);

		// Run classification
		File results = snomedReasonerService.classify("", new FileInputStream(baseRF2SnapshotZip), new FileInputStream(newDiabetesConceptDeltaZip), Application.DEFAULT_REASONER_FACTORY);

		// Assert results
		List<String> lines = readInferredRelationshipLinesTrim(results);
		assertEquals(3, lines.size());
		assertTrue(lines.contains("1\t\t73211009\t362969004\t0\t" + Concepts.IS_A + "\t900000000000011006\t900000000000451002"));
		assertTrue(lines.contains("1\t\t73211009\t113331007\t0\t" + FINDING_SITE + "\t900000000000011006\t900000000000451002"));
	}

}
