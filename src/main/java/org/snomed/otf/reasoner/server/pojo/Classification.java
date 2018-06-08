package org.snomed.otf.reasoner.server.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class Classification {
	private final String classificationId;
	private final String branch;
	private final String reasonerId;
	private final Date created;
	private final Set<String> previousReleases;
	private ClassificationStatus status;
	private String errorMessage;
	private String developerMessage;

	public Classification(Set<String> previousReleases, String branch, String reasonerId) {
		this.previousReleases = previousReleases;
		this.classificationId = UUID.randomUUID().toString();
		this.branch = branch;
		this.reasonerId = reasonerId;
		status = ClassificationStatus.SCHEDULED;
		created = new Date();
	}

	public String getClassificationId() {
		return classificationId;
	}

	public ClassificationStatus getStatus() {
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

	@JsonIgnore
	public Set<String> getPreviousReleases() {
		return previousReleases;
	}

	@JsonProperty("previousRelease")
	public String getPreviousReleaseSetString() {
		return previousReleases != null ? previousReleases.toString() : null;
	}

	public void setStatus(ClassificationStatus status) {
		this.status = status;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setDeveloperMessage(String developerMessage) {
		this.developerMessage = developerMessage;
	}

	public String getDeveloperMessage() {
		return developerMessage;
	}

}
