package org.snomed.otf.reasoner.server.rest;

import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController()
@RequestMapping(value = "/classifications", produces = "application/json")
public class ClassificationController {

	// TODO: Replace dummy implementation
	private Map<String, Classification> classifications;

	@Autowired
	private TaskScheduler taskScheduler;

	public ClassificationController() {
		classifications = new HashMap<>();
	}

	@RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity createClassification(@RequestParam MultipartFile rf2Delta,
											   @RequestParam(required = false) String branch,
											   @RequestParam String reasonerId,
											   UriComponentsBuilder uriComponentsBuilder) {
		String classificationId = UUID.randomUUID().toString();
		Classification classification = new Classification(classificationId, branch, reasonerId);
		classifications.put(classificationId, classification);

		// TODO: Remove this dummy state transition
		taskScheduler.schedule(() -> classification.setStatus("RUNNING"), secondsInFuture(10));
		taskScheduler.schedule(() -> classification.setStatus("COMPLETED"), secondsInFuture(20));

		return ResponseEntity.created(uriComponentsBuilder.path("/classifications/{classificationId}")
				.buildAndExpand(classificationId).toUri()).build();
	}

	@RequestMapping(path = "/{classificationId}", method = RequestMethod.GET)
	public Classification getClassification(@PathVariable String classificationId) throws FileNotFoundException {
		Classification classification = classifications.get(classificationId);
		if (classification == null) {
			throw new FileNotFoundException("Classification not found.");
		}
		return classification;
	}

	@RequestMapping(path = "/{classificationId}/results/rf2", method = RequestMethod.GET, produces="application/zip")
	public void getClassificationResultsRf2(@PathVariable String classificationId, HttpServletResponse response) throws IOException {
		Classification classification = classifications.get(classificationId);
		if (classification == null) {
			throw new FileNotFoundException("Classification not found.");
		}
		response.setHeader("Content-Disposition", "attachment; filename=\"classification-results.zip\"");
		ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
		putZipEntry(zipOutputStream,
				"RF2/sct2_Relationship_Delta_Classification_20170801.txt",
				"/dummy-results/sct2_Relationship_Delta_Classification_20170801.txt");
		putZipEntry(zipOutputStream,
				"RF2/der2_sRefset_EquivalentConceptSimpleMapDelta_INT_20180801.txt",
				"/dummy-results/der2_sRefset_EquivalentConceptSimpleMapDelta_INT_20180801.txt");
	}

	private void putZipEntry(ZipOutputStream zipOutputStream, String zipPath, String resourcePath) throws IOException {
		zipOutputStream.putNextEntry(new ZipEntry(zipPath));
		Streams.copy(
				getClass().getResourceAsStream(resourcePath),
				zipOutputStream, false);
	}

	public static final class Classification {
		private final String classificationId;
		private final String branch;
		private final String reasonerId;
		private final Date created;
		private String status;

		public Classification(String classificationId, String branch, String reasonerId) {
			this.classificationId = classificationId;
			this.branch = branch;
			this.reasonerId = reasonerId;
			status = "SCHEDULED";
			created = new Date();
		}

		public String getClassificationId() {
			return classificationId;
		}

		public String getStatus() {
			return status;
		}

		public Date getCreated() {
			return created;
		}

		public String getBranch() {
			return branch;
		}

		public String getReasonerId() {
			return reasonerId;
		}

		public void setStatus(String status) {
			this.status = status;
		}
	}

	private Date secondsInFuture(int seconds) {
		return new Date(new Date().getTime() + (1000 * seconds));
	}

}
