package org.snomed.otf.reasoner.server.service;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.snomed.otf.reasoner.server.Application;
import org.snomed.otf.reasoner.server.Configuration;
import org.snomed.otf.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.snomed.otf.reasoner.server.service.TestFileUtil.readLinesTrim;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(Configuration.class)
public class GCIClassificationIntegrationTest {

	@Autowired
	private SnomedReasonerService snomedReasonerService;

	@Test
	public void testClassifyGCI() throws IOException, OWLOntologyCreationException, ReleaseImportException, ReasonerServiceException {
		File baseRF2SnapshotZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Base_snapshot");
		File deltaZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Secondary_Diabetes_GCI_delta");
		assertNotNull(snomedReasonerService);

		// Run classification
		File results = snomedReasonerService.classify("", new FileInputStream(baseRF2SnapshotZip), new FileInputStream(deltaZip), Application.DEFAULT_REASONER_FACTORY);

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

}
