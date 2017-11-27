package org.snomed.otf.reasoner.server.service;

import org.snomed.otf.util.ZipUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class TestFileUtil {

	static List<String> readInferredRelationshipLinesTrim(File zipFile) throws IOException {
		return readLinesTrim(zipFile, "sct2_Relationship_Delta_Classification_");
	}

	static List<String> readEquivalentConceptLinesTrim(File zipFile) throws IOException {
		return readLinesTrim(zipFile, "der2_sRefset_EquivalentConceptSimpleMapDelta_Classification_");
	}

	private static List<String> readLinesTrim(File zipFile, String zipEntryNamePrefix) throws IOException {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(ZipUtil.getZipEntryStreamOrThrow(zipFile, zipEntryNamePrefix)))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line.trim());
				System.out.println(line);
			}
		}
		return lines;
	}
}
