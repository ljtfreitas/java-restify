package com.restify.http.client.response;

import com.restify.http.client.Headers;

public class RestifyEndpointResponseException extends RestifyHttpMessageReadException {

	private static final long serialVersionUID = 1L;

	private final StatusCode statusCode;
	private final Headers headers;
	private final String body;

	public RestifyEndpointResponseException(String message, StatusCode statusCode, Headers headers, String body) {
		super(message);
		this.statusCode = statusCode;
		this.headers = headers;
		this.body = body;
	}

	public StatusCode getStatusCode() {
		return statusCode;
	}

	public Headers getHeaders() {
		return headers;
	}

	public String getBody() {
		return body;
	}
}
