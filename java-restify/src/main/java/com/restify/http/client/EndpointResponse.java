package com.restify.http.client;

import java.io.Closeable;
import java.io.InputStream;

public abstract class EndpointResponse implements HttpResponseMessage, Closeable {

	private final EndpointResponseCode code;
	private final Headers headers;
	private final InputStream stream;

	public EndpointResponse(EndpointResponseCode code, Headers headers, InputStream stream) {
		this.code = code;
		this.headers = headers;
		this.stream = stream;
	}

	public Headers headers() {
		return headers;
	}

	public EndpointResponseCode code() {
		return code;
	}

	@Override
	public InputStream input() {
		return stream;
	}

}
