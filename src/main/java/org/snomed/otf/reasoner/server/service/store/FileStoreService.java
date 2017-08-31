package org.snomed.otf.reasoner.server.service.store;

import org.snomed.otf.reasoner.server.pojo.Classification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.text.SimpleDateFormat;

@Service
public class FileStoreService {

	private File storeRootDirectory;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final String INPUT_DELTA_ZIP = "input/delta-rf2.zip";
	private static final String RESULTS_ZIP = "output/classification-results-rf2.zip";

	public FileStoreService(@Value("${store.root-directory}") String storeRootDirectoryPath) {
		this.storeRootDirectory = new File(storeRootDirectoryPath);
	}

	public void saveDeltaInput(Classification classification, InputStream snomedRf2SnapshotArchive) throws IOException {
		StreamUtils.copy(snomedRf2SnapshotArchive, new FileOutputStream(getFile(classification, INPUT_DELTA_ZIP)));
	}

	public InputStream loadDeltaInput(Classification classification) throws FileNotFoundException {
		return new FileInputStream(getFile(classification, INPUT_DELTA_ZIP));
	}

	public void saveResults(Classification classification, File resultsArchive) throws IOException {
		try (FileInputStream fileInputStream = new FileInputStream(resultsArchive)) {
			StreamUtils.copy(fileInputStream, new FileOutputStream(getFile(classification, RESULTS_ZIP)));
		}
	}

	public InputStream loadResults(Classification classification) throws FileNotFoundException {
		return new FileInputStream(getFile(classification, RESULTS_ZIP));
	}

	private File getFile(Classification classification, String relativePath) {
		File file = new File(storeRootDirectory, "classifications/" + DATE_FORMAT.format(classification.getCreated()) + "/" + classification.getClassificationId() + "/" + relativePath);
		if (!file.exists()) {
			// Attempt to make directories
			file.getParentFile().mkdirs();
		}
		return file;
	}

	public InputStream loadPreviousRelease(String previousRelease) throws FileNotFoundException {
		File file = new File(storeRootDirectory, "releases/" + previousRelease);
		if (!file.isFile()) {
			throw new FileNotFoundException("Previous release file not found '" + previousRelease + "'");
		}
		return new FileInputStream(file);
	}
}
