package org.snomed.otf.reasoner.server.service;

public class ReasonerServiceRuntimeException extends RuntimeException {
	public ReasonerServiceRuntimeException(String message) {
		super(message);
	}

	public ReasonerServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
