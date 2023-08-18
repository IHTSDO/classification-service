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

import static org.snomed.otf.owltoolkit.service.SnomedReasonerService.ELK_REASONER_FACTORY;

@RestController()
@RequestMapping(value = "/classifications", produces = "application/json")
public class ClassificationController {

	@Autowired
	private ClassificationJobManager classificationJobManager;

	@RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data")
	@ApiOperation(value = "Create and run a classification job.",
			notes = "The dependencyPackage is only required for classifying extensions."
			+ " The value should be the international release package that the extension is based on." +
					"Include a responseMessageQueue if you want to receive job status updates via JMS. " +
					"The branch parameter is deprecated and will be removed in a future release.")
	public ResponseEntity<?> createClassification(@RequestParam(required = false) String previousPackage,
			@RequestParam(required = false) String dependencyPackage,
			@RequestParam MultipartFile rf2Delta,
			@RequestParam(required = false) String responseMessageQueue,
			@RequestParam(required = false) @Deprecated String branch,
			@RequestParam(defaultValue = ELK_REASONER_FACTORY) String reasonerId,
			UriComponentsBuilder uriComponentsBuilder) {

		if (Strings.isNullOrEmpty(previousPackage) && Strings.isNullOrEmpty(dependencyPackage)) {
			throw new IllegalArgumentException("Either the previousPackage or dependencyPackage parameter must be given.");
		}
		Classification classification;
		try {
			classification = classificationJobManager.queueClassification(previousPackage, dependencyPackage, rf2Delta.getInputStream(), reasonerId, responseMessageQueue, branch);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to persist RF2 delta archive", e);
		}

		return ResponseEntity.created(uriComponentsBuilder.path("/classifications/{classificationId}")
				.buildAndExpand(classification.getClassificationId()).toUri()).build();
	}

	@RequestMapping(path = "/{classificationId}", method = RequestMethod.GET)
	@ApiOperation("Check the status of an existing classification.")
	public Classification getClassification(@PathVariable String classificationId) throws FileNotFoundException {
		Classification classification = classificationJobManager.getClassification(classificationId);
		if (classification == null) {
			throw new FileNotFoundException("Classification not found.");
		}
		return classification;
	}

	@RequestMapping(path = "/{classificationId}/results/rf2", method = RequestMethod.GET, produces="application/zip")
	@ApiOperation("Download the results of a completed classification.")
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
