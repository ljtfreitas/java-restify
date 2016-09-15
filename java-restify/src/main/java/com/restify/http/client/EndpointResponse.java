package com.restify.http.client;

import static com.restify.http.client.Headers.CONTENT_LENGTH;

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

	@Override
	public Headers headers() {
		return headers;
	}

	@Override
	public EndpointResponseCode code() {
		return code;
	}

	@Override
	public InputStream body() {
		return stream;
	}

	public boolean readable() {
		return statusIsReadable() && hasContentLength();
	}

	private boolean statusIsReadable() {
		return !(code.isInformational() || code.isNoContent() || code.isNotModified());
	}

	private boolean hasContentLength() {
		int contentLength = headers.get(CONTENT_LENGTH).map(h -> Integer.valueOf(h.value())).orElse(-1);
		return contentLength != 0;
	}
}
