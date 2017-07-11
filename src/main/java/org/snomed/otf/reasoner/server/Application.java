package org.snomed.otf.reasoner.server;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class Application {

	// Quick method for manual testing
	public static void main(String[] args) throws ReleaseImportException, OWLOntologyCreationException {

		// Path to a SNOMED release on local disk
		String releaseDirectoryPath = "release/SnomedCT_InternationalRF2_Production_20170131";

		// Name of Reasoner factory to use on classpath
		String reasonerFactoryClassName = "org.semanticweb.elk.owlapi.ElkReasonerFactory";

		new SnomedReasonerService().classify(releaseDirectoryPath, reasonerFactoryClassName);
	}

}
