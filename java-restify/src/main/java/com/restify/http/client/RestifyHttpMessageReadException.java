package com.restify.http.client;

import com.restify.http.RestifyHttpException;

public class RestifyHttpMessageReadException extends RestifyHttpException {

	private static final long serialVersionUID = 1L;

	public RestifyHttpMessageReadException(String message) {
		super(message);
	}

	public RestifyHttpMessageReadException(Throwable cause) {
		super(cause);
	}

	public RestifyHttpMessageReadException(String message, Throwable cause) {
		super(message, cause);
	}


}
