package org.snomed.otf.reasoner.server.rest;

import com.google.common.base.Strings;
import io.swagger.annotations.ApiOperation;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.snomed.otf.reasoner.server.pojo.Classification;
import org.snomed.otf.reasoner.server.service.ClassificationJobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.snomed.otf.owltoolkit.service.SnomedReasonerService.ELK_REASONER_FACTORY;

@RestController()
@RequestMapping(value = "/classifications", produces = "application/json")
public class ClassificationController {

	@Autowired
	private ClassificationJobManager classificationJobManager;

	@RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity createClassification(@RequestParam(required = false) String previousRelease,
			@RequestParam(required = false) Set<String> previousReleases,
			@RequestParam MultipartFile rf2Delta,
			@RequestParam(required = false) @Deprecated String branch,
			@RequestParam(defaultValue = ELK_REASONER_FACTORY) String reasonerId,
			UriComponentsBuilder uriComponentsBuilder) {

		if (Strings.isNullOrEmpty(previousRelease) && (previousReleases == null || previousReleases.isEmpty())) {
			throw new IllegalArgumentException("Either the 'previousRelease' or 'previousReleases' parameter must be given.");
		}

		if (previousReleases == null) {
			previousReleases = new HashSet<>();
		}

		// Comma split 'previousRelease' and add to 'previousReleases'
		if (!Strings.isNullOrEmpty(previousRelease)) {
			String[] split = previousRelease.split("\\,");
			for (String s : split) {
				s = s.trim();
				if (!s.isEmpty()) {
					previousReleases.add(s);
				}
			}
		}

		Classification classification;
		try {
			classification = classificationJobManager.queueClassification(previousReleases, rf2Delta.getInputStream(), reasonerId, branch);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to persist RF2 delta archive", e);
		}

		return ResponseEntity.created(uriComponentsBuilder.path("/classifications/{classificationId}")
				.buildAndExpand(classification.getClassificationId()).toUri()).build();
	}

	@RequestMapping(path = "/{classificationId}", method = RequestMethod.GET)
	public Classification getClassification(@PathVariable String classificationId) throws FileNotFoundException {
		Classification classification = classificationJobManager.getClassification(classificationId);
		if (classification == null) {
			throw new FileNotFoundException("Classification not found.");
		}
		return classification;
	}

	@RequestMapping(path = "/{classificationId}/results/rf2", method = RequestMethod.GET, produces="application/zip")
	public void getClassificationResultsRf2(@PathVariable String classificationId, HttpServletResponse response) throws IOException {
		Classification classification = classificationJobManager.getClassification(classificationId);
		if (classification == null) {
			throw new FileNotFoundException("Classification not found.");
		}
		response.setHeader("Content-Disposition", "attachment; filename=\"classification-results.zip\"");

		try (InputStream classificationResults = classificationJobManager.getClassificationResults(classification)) {
			Streams.copy(classificationResults, response.getOutputStream(), false);
		}
	}

}
