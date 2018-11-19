package org.snomed.otf.reasoner.server.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

public final class Classification {
	private final String classificationId;
	private final String branch;
	private final String reasonerId;
	private final Date created;
	private final String previousPackage;
	private final String dependencyPackage;
	private ClassificationStatus status;
	private String statusMessage;

	public Classification(String previousPackage,  String dependencyPackage, String branch, String reasonerId) {
		this.previousPackage = previousPackage;
		this.dependencyPackage = dependencyPackage;
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

	public String getPreviousPackage() {
		return previousPackage;
	}

	public String getDependencyPackage() {
		return dependencyPackage;
	}

	public void setStatus(ClassificationStatus status) {
		this.status = status;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

}
