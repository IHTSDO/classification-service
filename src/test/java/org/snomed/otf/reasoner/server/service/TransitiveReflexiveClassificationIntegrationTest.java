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
public class TransitiveReflexiveClassificationIntegrationTest {

	@Autowired
	private SnomedReasonerService snomedReasonerService;

	@Test
	public void testClassify() throws IOException, OWLOntologyCreationException, ReleaseImportException, ReasonerServiceException {
		File baseRF2SnapshotZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Base_snapshot");
		File deltaZip = ZipUtil.zipDirectoryRemovingCommentsAndBlankLines("src/test/resources/SnomedCT_MiniRF2_Anatomy_Transitive_Reflexive_delta");
		assertNotNull(snomedReasonerService);

		// Run classification
		File results = snomedReasonerService.classify("", new FileInputStream(baseRF2SnapshotZip), new FileInputStream(deltaZip), Application.DEFAULT_REASONER_FACTORY);

		// Assert results
		List<String> lines = readInferredRelationshipLinesTrim(results);

		// This inference is a direct result of reflection
		// because we stated that Structure_of_upper_limb - All_or_Part_of - Entire_upper_limb
		// and that "All_or_Part_of" is reflexive. The attribute has been reflected onto Entire_upper_limb
		assertTrue("Inferred relationship. Entire_upper_limb - All_or_Part_of - Entire_upper_limb",
				lines.contains("1		500003001	500003001	0	733928003	900000000000011006	900000000000451002"));

		// The attribute added to Entire_upper_limb via reflection causes the concept to be subsumed by Structure_of_upper_limb
		// which is defined as a subclass of Body_structure and All_or_Part_of - Entire_upper_limb
		assertTrue("Inferred relationship. Entire_upper_limb - is a - Structure_of_upper_limb",
				lines.contains("1		500003001	500006001	0	116680003	900000000000011006	900000000000451002"));

		assertTrue("Inferred relationship. Structure_of_hand - is a - Structure_of_upper_limb",
				lines.contains("1		500007001	500006001	0	116680003	900000000000011006	900000000000451002"));
		assertTrue("Redundant relationship. Structure_of_hand - is a - Body_structure",
				lines.contains("200112001		0		500007001	500001001	0	116680003	900000000000011006	900000000000451002"));

		assertTrue("Inferred relationship. Entire_hand - All_or_Part_of - Entire_hand",
				lines.contains("1		500004001	500004001	0	733928003	900000000000011006	900000000000451002"));
		assertTrue("Inferred relationship. Entire_hand - is a - Structure_of_hand",
				lines.contains("1		500004001	500007001	0	116680003	900000000000011006	900000000000451002"));

		assertTrue("Inferred relationship. Structure_of_finger - is a - Structure_of_hand",
				lines.contains("1		500008001	500007001	0	116680003	900000000000011006	900000000000451002"));
		assertTrue("Redundant relationship. Structure_of_finger - is a - Structure_of_hand",
				lines.contains("200114001		0		500008001	500001001	0	116680003	900000000000011006	900000000000451002"));

		assertTrue("Inferred relationship. Entire_finger - All_or_Part_of - Entire_finger",
				lines.contains("1		500005001	500005001	0	733928003	900000000000011006	900000000000451002"));
		assertTrue("Inferred relationship. Entire_finger - is a - Structure_of_finger",
				lines.contains("1		500005001	500008001	0	116680003	900000000000011006	900000000000451002"));

		// Although these relationships would normally be inherited from supertypes they should not be part of the normal form
		// because more specific attributes are present, as identified via the Part_of transitive closure
		// A new transitive closure graph is built for each transitive attribute type and it's subtypes
		assertFalse("Should not be part of the normal form: Structure_of_finger - All_or_Part_of - Entire_upper_limb",
				lines.contains("1		500008001	500003001	0	733928003	900000000000011006	900000000000451002"));
		assertFalse("Should not be part of the normal form: Structure_of_finger - All_or_Part_of - Entire_upper_limb",
				lines.contains("1		500008001	500004001	0	733928003	900000000000011006	900000000000451002"));
		assertFalse("Should not be part of the normal form: Entire_finger - All_or_Part_of - Entire_upper_limb",
				lines.contains("1		500005001	500003001	0	733928003	900000000000011006	900000000000451002"));

		assertEquals(11, lines.size());
	}

}
