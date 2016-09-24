package com.restify.http.client.request;

import com.restify.http.RestifyHttpException;

public class RestifyHttpMessageWriteException extends RestifyHttpException {

	private static final long serialVersionUID = 1L;

	public RestifyHttpMessageWriteException(String message) {
		super(message);
	}

	public RestifyHttpMessageWriteException(Throwable cause) {
		super(cause);
	}

	public RestifyHttpMessageWriteException(String message, Throwable cause) {
		super(message, cause);
	}

}
