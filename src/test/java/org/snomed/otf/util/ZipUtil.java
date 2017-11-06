package org.snomed.otf.util;

import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
	public static File zipDirectory(String directoryPath) throws IOException {
		File directory = new File(directoryPath);
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("This is not a directory " + directory.getAbsolutePath());
		}
		Path zip = Files.createTempFile("zipped-directory_" + new Date().getTime(), "zip");
		File zipFile = zip.toFile();
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
			Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
					File file = path.toFile();
					zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
					try (FileInputStream in = new FileInputStream(file)) {
						StreamUtils.copy(in, zipOutputStream);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
		return zipFile;
	}
}
