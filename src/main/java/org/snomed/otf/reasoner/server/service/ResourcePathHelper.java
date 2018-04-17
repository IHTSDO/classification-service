package org.snomed.otf.reasoner.server.service;

import org.snomed.otf.reasoner.server.pojo.Classification;

import java.text.SimpleDateFormat;

public class ResourcePathHelper {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final String INPUT_DELTA_ZIP = "input/delta-rf2.zip";
	private static final String RESULTS_ZIP = "output/classification-results-rf2.zip";

	public static String getInputDeltaPath(Classification classification) {
		return getFilePath(classification, INPUT_DELTA_ZIP);
	}

	public static String getResultsPath(Classification classification) {
		return getFilePath(classification, RESULTS_ZIP);
	}

	private static String getFilePath(Classification classification, String relativePath) {
		return DATE_FORMAT.format(classification.getCreated()) + "/" + classification.getClassificationId() + "/" + relativePath;
	}
}
