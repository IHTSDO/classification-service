package org.snomed.otf.reasoner.server.pojo;

public class ClassificationStatusAndMessage {

	private ClassificationStatus status;
	private String statusMessage;

	public ClassificationStatusAndMessage() {
	}

	public ClassificationStatusAndMessage(ClassificationStatus status) {
		this.status = status;
	}

	public ClassificationStatusAndMessage(ClassificationStatus status, String statusMessage) {
		this.status = status;
		this.statusMessage = statusMessage;
	}

	public ClassificationStatus getStatus() {
		return status;
	}

	public void setStatus(ClassificationStatus status) {
		this.status = status;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	@Override
	public String toString() {
		return "ClassificationStatusAndMessage{" +
				"status=" + status +
				", statusMessage='" + statusMessage + '\'' +
				'}';
	}
}
