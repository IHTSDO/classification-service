package org.snomed.otf.reasoner.server.rest;

import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.snomed.otf.reasoner.server.pojo.Classification;
import org.snomed.otf.reasoner.server.service.SnomedReasonerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.snomed.otf.reasoner.server.Application.DEFAULT_REASONER_FACTORY;

@RestController()
@RequestMapping(value = "/classifications", produces = "application/json")
public class ClassificationController {

	@Autowired
	private SnomedReasonerService snomedReasonerService;

	@RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity createClassification(@RequestParam String previousRelease,
											   @RequestParam MultipartFile rf2Delta,
											   @RequestParam(required = false) String branch,
											   @RequestParam(defaultValue = DEFAULT_REASONER_FACTORY) String reasonerId,
											   UriComponentsBuilder uriComponentsBuilder) {

		Classification classification;
		try {
			classification = snomedReasonerService.queueClassification(previousRelease, rf2Delta.getInputStream(), reasonerId, branch);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to persist RF2 delta archive", e);
		}

		return ResponseEntity.created(uriComponentsBuilder.path("/classifications/{classificationId}")
				.buildAndExpand(classification.getClassificationId()).toUri()).build();
	}

	@RequestMapping(path = "/{classificationId}", method = RequestMethod.GET)
	public Classification getClassification(@PathVariable String classificationId) throws FileNotFoundException {
		Classification classification = snomedReasonerService.getClassification(classificationId);
		if (classification == null) {
			throw new FileNotFoundException("Classification not found.");
		}
		return classification;
	}

	@RequestMapping(path = "/{classificationId}/results/rf2", method = RequestMethod.GET, produces="application/zip")
	public void getClassificationResultsRf2(@PathVariable String classificationId, HttpServletResponse response) throws IOException {
		Classification classification = snomedReasonerService.getClassification(classificationId);
		if (classification == null) {
			throw new FileNotFoundException("Classification not found.");
		}
		response.setHeader("Content-Disposition", "attachment; filename=\"classification-results.zip\"");

		try (InputStream classificationResults = snomedReasonerService.getClassificationResults(classification)) {
			Streams.copy(classificationResults, response.getOutputStream(), false);
		}
	}

}
