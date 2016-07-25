package com.restify.http;

public class RestifyHttpException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RestifyHttpException(Throwable cause) {
		super(cause);
	}

	public RestifyHttpException(String message) {
		super(message);
	}

	public RestifyHttpException(String message, Throwable cause) {
		super(message, cause);
	}

}
