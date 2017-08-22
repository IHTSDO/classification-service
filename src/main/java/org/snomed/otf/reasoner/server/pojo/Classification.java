package org.snomed.otf.reasoner.server.pojo;

import java.util.Date;
import java.util.UUID;

public final class Classification {
	private final String classificationId;
	private final String branch;
	private final String reasonerId;
	private final Date created;
	private ClassificationStatus status;
	private String errorMessage;
	private String developerMessage;

	public Classification(String branch, String reasonerId) {
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
