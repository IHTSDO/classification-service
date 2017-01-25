package org.snomed.otf.reasoner.server;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;

public class App {

	public static void main(String[] args) throws ReleaseImportException {
		new SnomedReasonerService().classify();
	}

}
