package org.snomed.otf.reasoner.server.service;

import org.snomed.otf.util.ZipUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TestFileUtil {

	public static List<String> readLinesTrim(File results) throws IOException {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(ZipUtil.getZipEntryStreamOrThrow(results, "sct2_Relationship_Delta_Classification_")))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line.trim());
				System.out.println(line);
			}
		}
		return lines;
	}
}
