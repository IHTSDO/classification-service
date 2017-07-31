package org.snomed.otf.reasoner.server.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {

	private static final Logger logger = LoggerFactory.getLogger(ControllerAdvice.class);
	private static final String ERROR = "error";
	private static final String MESSAGE = "message";

	@ExceptionHandler({
			IllegalArgumentException.class,
			IllegalStateException.class,
			HttpRequestMethodNotSupportedException.class,
			HttpMediaTypeNotSupportedException.class
	})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public Map<String,Object> handleIllegalArgumentException(Exception exception) {
		HashMap<String, Object> result = new HashMap<>();
		result.put(ERROR, HttpStatus.BAD_REQUEST);
		result.put(MESSAGE, exception.getMessage());
		logger.info("bad request {}", exception.getMessage(), exception);
		return result;
	}

	@ExceptionHandler(FileNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public Map<String,Object> handleNotFoundException(Exception exception) {
		HashMap<String, Object> result = new HashMap<>();
		result.put(ERROR, HttpStatus.NOT_FOUND);
		result.put(MESSAGE, exception.getMessage());
		return result;
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public Map<String,Object> handleException(Exception exception) {
		logger.error("Unhandled exception.", exception);
		HashMap<String, Object> result = new HashMap<>();
		result.put(ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		result.put(MESSAGE, exception.getMessage());
		return result;
	}

}
