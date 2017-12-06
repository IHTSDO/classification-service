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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.snomed.otf.reasoner.server.service.TestFileUtil.readEquivalentConceptLinesTrim;
import static org.snomed.otf.reasoner.server.service.TestFileUtil.readInferredRelationshipLinesTrim;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(Configuration.class)
public class EquivalenceClassificationIntegrationTest {

	@Autowired
	private SnomedReasonerService snomedReasonerService;

	private static final String FINDING_SITE = "363698007";

	/**
	 * The delta contains a clone of 362969004 | Disorder of endocrine system |
	 * We expect the original concept and the clone to be returned in the equivalent concept reference set
	 */
	@Test
	public void testClassify() throws IOException, OWLOntologyCreationException, ReleaseImportException, ReasonerServiceException {
		File baseRF2SnapshotZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Base_snapshot");
		File emptyDeltaZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Equivalence_delta");
		assertNotNull(snomedReasonerService);

		// Run classification
		File results = snomedReasonerService.classify("", new FileInputStream(baseRF2SnapshotZip), new FileInputStream(emptyDeltaZip), Application.DEFAULT_REASONER_FACTORY);

		// Assert results
		List<String> equivalentConceptLines = readEquivalentConceptLinesTrim(results);
		assertEquals("EquivalentConcept delta should contain a pair of concept.", 3, equivalentConceptLines.size());

		String disorderOfEndocrineSystem = "362969004";
		String disorderOfEndocrineSystemClone = "1362969004";

		assertEquals("There should be an equivalent concept member containing the original concept.",
				1, equivalentConceptLines.stream().filter(line -> line.contains("\t1\t\t\t" + disorderOfEndocrineSystem + "\t")).count());
		assertEquals("There should be an equivalent concept member containing the cloned concept.",
				1, equivalentConceptLines.stream().filter(line -> line.contains("\t1\t\t\t" + disorderOfEndocrineSystemClone + "\t")).count());

		List<String> relationshipLines = readInferredRelationshipLinesTrim(results);
		assertEquals("Relationship delta should contain new inferences.", 5, relationshipLines.size());
	}

}
