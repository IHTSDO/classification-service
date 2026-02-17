package org.snomed.otf.reasoner.server.rest;

import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.reasoner.server.configuration.ApplicationProperties;
import org.snomed.otf.reasoner.server.pojo.Classification;
import org.snomed.otf.reasoner.server.service.ClassificationJobManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.snomed.otf.owltoolkit.service.SnomedReasonerService.ELK_REASONER_FACTORY;

@RestController()
@RequestMapping(value = "/classifications", produces = "application/json")
@Tag(name = "Classifications")
public class ClassificationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationController.class);

	private final ClassificationJobManager classificationJobManager;
	private final ApplicationProperties applicationProperties;

	public ClassificationController(ClassificationJobManager classificationJobManager, ApplicationProperties applicationProperties) {
		this.classificationJobManager = classificationJobManager;
		this.applicationProperties = applicationProperties;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data")
	@Operation(summary = "Create and run a classification job.",
			description = "The dependencyPackage is only required for classifying extensions."
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

		throwIfIllegalArguments(previousPackage, dependencyPackage);

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
	@Operation(summary = "Check the status of an existing classification.")
	public Classification getClassification(@PathVariable String classificationId) throws FileNotFoundException {
		Classification classification = classificationJobManager.getClassification(classificationId);
		if (classification == null) {
			throw new FileNotFoundException("Classification not found.");
		}
		return classification;
	}

	@RequestMapping(path = "/{classificationId}/results/rf2", method = RequestMethod.GET, produces="application/zip")
	@Operation(summary = "Download the results of a completed classification.")
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

	private void throwIfIllegalArguments(String previousPackage, String dependencyPackage) {
		boolean legacyDependencyManagement = applicationProperties.isLegacyDependencyManagement();
		if (!legacyDependencyManagement) {
			LOGGER.trace("Skipping parameter check as using NEW technique (via given MDRS)");
			return;
		}

		if (Strings.isNullOrEmpty(previousPackage) && Strings.isNullOrEmpty(dependencyPackage)) {
			throw new IllegalArgumentException("Either the previousPackage or dependencyPackage parameter must be given.");
		}
	}
}
