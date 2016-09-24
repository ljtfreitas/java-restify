package com.restify.http.client.response;

import com.restify.http.client.Headers;

public class EndpointResponse<T> {

	private final EndpointResponseCode code;
	private final Headers headers;
	private final T body;

	public EndpointResponse(EndpointResponseCode code, Headers headers, T body) {
		this.code = code;
		this.headers = headers;
		this.body = body;
	}

	public Headers headers() {
		return headers;
	}

	public EndpointResponseCode code() {
		return code;
	}

	public T body() {
		return body;
	}
}
