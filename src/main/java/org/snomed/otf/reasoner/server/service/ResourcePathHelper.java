package org.snomed.otf.reasoner.server.service;

import org.snomed.otf.reasoner.server.pojo.Classification;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ResourcePathHelper {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final String INPUT_DELTA_ZIP = "input/delta-rf2.zip";
	private static final String RESULTS_ZIP = "output/classification-results-rf2.zip";
	public static final String CLASSIFICATION_JSON = "classification.json";

	public static String getClassificationPathFromToday(String classificationId) {
		return getFilePath(new Date(), classificationId, CLASSIFICATION_JSON);
	}

	public static String getClassificationPathFromPast(String classificationId, int daysInPast) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.add(Calendar.DATE, -daysInPast);
		return getFilePath(calendar.getTime(), classificationId, CLASSIFICATION_JSON);
	}

	public static String getInputDeltaPath(Classification classification) {
		return getFilePath(classification, INPUT_DELTA_ZIP);
	}

	public static String getResultsPath(Classification classification) {
		return getFilePath(classification, RESULTS_ZIP);
	}

	private static String getFilePath(Classification classification, String relativePath) {
		Date created = classification.getCreated();
		String classificationId = classification.getClassificationId();
		return getFilePath(created, classificationId, relativePath);
	}

	private static String getFilePath(Date created, String classificationId, String relativePath) {
		return DATE_FORMAT.format(created) + "/" + classificationId + "/" + relativePath;
	}
}
