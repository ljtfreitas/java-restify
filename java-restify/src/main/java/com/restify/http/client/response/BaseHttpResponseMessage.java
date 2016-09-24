package com.restify.http.client.response;

import static com.restify.http.client.Headers.CONTENT_LENGTH;

import java.io.InputStream;

import com.restify.http.client.Headers;

public abstract class BaseHttpResponseMessage implements HttpResponseMessage {

	private final EndpointResponseCode code;
	private final Headers headers;
	private final InputStream body;

	protected BaseHttpResponseMessage(EndpointResponseCode code, Headers headers, InputStream body) {
		this.code = code;
		this.headers = headers;
		this.body = body;
	}
	
	@Override
	public EndpointResponseCode code() {
		return code;
	}
	
	@Override
	public Headers headers() {
		return headers;
	}
	
	@Override
	public InputStream body() {
		return body;
	}
	
	@Override
	public boolean readable() {
		return readableStatus() && hasContentLength();
	}
	
	private boolean readableStatus() {
		return !(code.isInformational() || code.isNoContent() || code.isNotModified());
	}

	private boolean hasContentLength() {
		int contentLength = headers.get(CONTENT_LENGTH).map(h -> Integer.valueOf(h.value())).orElse(-1);
		return contentLength != 0;
	}
}
