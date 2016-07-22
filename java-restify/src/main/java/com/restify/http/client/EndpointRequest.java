package com.restify.http.client;

import java.net.URL;
import java.util.Optional;

public class EndpointRequest {

	private final URL endpoint;
	private final String method;
	private final Headers headers;
	private final Object body;
	private final Class<?> expectedType;

	public EndpointRequest(URL endpoint, String method, Headers headers, Object body, Class<?> expectedType) {
		this.endpoint = endpoint;
		this.method = method;
		this.headers = headers;
		this.body = body;
		this.expectedType = expectedType;
	}

	public URL endpoint() {
		return endpoint;
	}

	public String method() {
		return method;
	}

	public Optional<Object> body() {
		return Optional.ofNullable(body);
	}

	public Headers headers() {
		return headers;
	}

	public Class<?> expectedType() {
		return expectedType;
	}

}
