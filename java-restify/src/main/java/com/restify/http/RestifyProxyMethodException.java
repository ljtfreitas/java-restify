package com.restify.http;

public class RestifyProxyMethodException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RestifyProxyMethodException(Throwable cause) {
		super(cause);
	}

	public RestifyProxyMethodException(String message, Throwable cause) {
		super(message, cause);
	}

}
