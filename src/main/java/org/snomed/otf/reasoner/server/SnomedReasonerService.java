package org.snomed.otf.reasoner.server;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.snomed.otf.reasoner.server.taxonomy.ReasonerTaxonomyBuilder;

public class SnomedReasonerService {

	public void classify() throws ReleaseImportException {
		ReasonerTaxonomyBuilder reasonerTaxonomyBuilder = new ReasonerTaxonomyBuilder();
		reasonerTaxonomyBuilder.build();
	}

}
