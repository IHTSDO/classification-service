package org.snomed.otf.reasoner.server.service;

public class ReasonerServiceException extends Exception {
	public ReasonerServiceException(String message) {
		super(message);
	}

	public ReasonerServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
