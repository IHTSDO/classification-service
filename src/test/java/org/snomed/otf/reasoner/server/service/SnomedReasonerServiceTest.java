package org.snomed.otf.reasoner.server.service;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.snomed.otf.reasoner.server.Configuration;
import org.snomed.otf.reasoner.server.service.constants.Concepts;
import org.snomed.otf.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(Configuration.class)
public class SnomedReasonerServiceTest {

	@Autowired
	private SnomedReasonerService snomedReasonerService;

	private static final String REASONER_FACTORY_CLASS_NAME = "org.semanticweb.elk.owlapi.ElkReasonerFactory";
	private static final String FINDING_SITE = "363698007";

	@Test
	public void testClassifyBase() throws IOException, OWLOntologyCreationException, ReleaseImportException, ReasonerServiceException {
		File baseRF2SnapshotZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Base_snapshot");
		File emptyDeltaZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Empty_delta");
		assertNotNull(snomedReasonerService);

		// Run classification
		File results = snomedReasonerService.classify(new FileInputStream(baseRF2SnapshotZip), new FileInputStream(emptyDeltaZip), REASONER_FACTORY_CLASS_NAME);

		// Assert results
		List<String> lines = readLinesTrim(results);
		assertEquals("Relationship delta should only contain the header line.",1, lines.size());
	}

	@Test
	public void testClassifyNewConcept() throws IOException, OWLOntologyCreationException, ReleaseImportException, ReasonerServiceException {
		File baseRF2SnapshotZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Base_snapshot");
		File newDiabetesConceptDeltaZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Add_Diabetes_delta");
		assertNotNull(snomedReasonerService);

		// Run classification
		File results = snomedReasonerService.classify(new FileInputStream(baseRF2SnapshotZip), new FileInputStream(newDiabetesConceptDeltaZip), REASONER_FACTORY_CLASS_NAME);

		// Assert results
		List<String> lines = readLinesTrim(results);
		assertEquals(3, lines.size());
		assertTrue(lines.contains("1\t\t73211009\t362969004\t0\t" + Concepts.IS_A + "\t900000000000011006\t900000000000451002"));
		assertTrue(lines.contains("1\t\t73211009\t113331007\t0\t" + FINDING_SITE + "\t900000000000011006\t900000000000451002"));
	}

	@Test
	public void testClassifyGCI() throws IOException, OWLOntologyCreationException, ReleaseImportException, ReasonerServiceException {
		File baseRF2SnapshotZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Base_snapshot");
		File deltaZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Secondary_Diabetes_GCI_delta");
		assertNotNull(snomedReasonerService);

		// Run classification
		File results = snomedReasonerService.classify(new FileInputStream(baseRF2SnapshotZip), new FileInputStream(deltaZip), REASONER_FACTORY_CLASS_NAME);

		// Assert results
		List<String> lines = readLinesTrim(results);
		assertEquals(9, lines.size());

		assertTrue("Redundant relationship. Diabetes caused by drug - Is a - Diabetes mellitus",
				lines.contains("200107001\t\t0\t\t100103001\t73211009\t0\t116680003\t900000000000011006\t900000000000451002"));

		assertTrue("Redundant relationship. Diabetes due to cystic fibrosis - Is a - Diabetes mellitus",
				lines.contains("200107001\t\t0\t\t100103001\t73211009\t0\t116680003\t900000000000011006\t900000000000451002"));

		assertTrue("Inferred relationship. Diabetes caused by drug - Is a - Secondary diabetes mellitus",
				lines.contains("1\t\t100103001\t8801005\t0\t116680003\t900000000000011006\t900000000000451002"));

		assertTrue("Inferred relationship. Diabetes due to cystic fibrosis - Is a - Secondary diabetes mellitus",
				lines.contains("1\t\t100104001\t8801005\t0\t116680003\t900000000000011006\t900000000000451002"));

		// "Diabetes caused by drug" and "Diabetes due to cystic fibrosis" are classified as a type of "Secondary diabetes mellitus"
		// This proves that the CGI axioms from the OWL Axiom reference set are working.



		// There are some other inferences too:
		assertTrue("Inferred relationship. Diabetes mellitus - Finding site - Structure of endocrine system",
				lines.contains("1\t\t73211009\t113331007\t0\t363698007\t900000000000011006\t900000000000451002"));

		assertTrue("Inferred relationship. Secondary diabetes mellitus - Finding site - Structure of endocrine system",
				lines.contains("1\t\t8801005\t113331007\t0\t363698007\t900000000000011006\t900000000000451002"));

		assertTrue("Inferred relationship. Diabetes caused by drug - Finding site - Structure of endocrine system",
				lines.contains("1\t\t100103001\t113331007\t0\t363698007\t900000000000011006\t900000000000451002"));

		assertTrue("Inferred relationship. Diabetes due to cystic fibrosis - Finding site - Structure of endocrine system",
				lines.contains("1\t\t100104001\t113331007\t0\t363698007\t900000000000011006\t900000000000451002"));

	}

	private List<String> readLinesTrim(File results) throws IOException {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(ZipUtil.getZipEntryStreamOrThrow(results, "sct2_Relationship_Delta_Classification_")))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line.trim());
				System.out.println(line);
			}
		}
		return lines;
	}

}
