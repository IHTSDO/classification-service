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
import static org.snomed.otf.reasoner.server.service.TestFileUtil.readInferredRelationshipLinesTrim;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(Configuration.class)
public class PropertyChainClassificationIntegrationTest {

	@Autowired
	private SnomedReasonerService snomedReasonerService;

	@Test
	public void testClassifyPropertyChain() throws IOException, OWLOntologyCreationException, ReleaseImportException, ReasonerServiceException {
		File baseRF2SnapshotZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Base_snapshot");
		File deltaZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Active_Ingredient_Property_Chain_delta");
		assertNotNull(snomedReasonerService);

		// Run classification
		File results = snomedReasonerService.classify("", new FileInputStream(baseRF2SnapshotZip), new FileInputStream(deltaZip), Application.DEFAULT_REASONER_FACTORY);

		// Assert results
		List<String> lines = readInferredRelationshipLinesTrim(results);
		assertEquals(3, lines.size());

		// This relationship is inferred by the property chain and leads to 'Morphine sulphate product' being subsumed by 'Morphine product'
		// however it should not be part of the normal form because:
		// although 'Morphine substance' is NOT a subclass of 'Morphine sulphate substance'
		// it is a modification of 'Morphine sulphate substance'
		// 'Is modification of' is a transitive attribute
		// This makes the stated relationships "Morphine sulphate product - Has active ingredient - Morphine sulphate substance" more specific.
		// This makes the relationship "Morphine sulphate product - Has active ingredient - Morphine substance" redundant.
		assertFalse("Inferred relationship. Morphine sulphate product - Has active ingredient - Morphine substance",
				lines.contains("1\t\t100206001\t100202001\t0\t127489000\t900000000000011006\t900000000000451002"));

		assertTrue("Inferred relationship. Morphine sulphate product - Is a - Morphine product",
				lines.contains("1\t\t100206001\t100205001\t0\t116680003\t900000000000011006\t900000000000451002"));

		assertTrue("Redundant relationship. Morphine sulphate product - Is a - Product",
				lines.contains("200210021\t\t0\t\t100206001\t100204001\t0\t116680003\t900000000000011006\t900000000000451002"));

	}

}
