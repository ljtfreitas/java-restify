package com.restify.http.client.response;

import static com.restify.http.client.Headers.CONTENT_LENGTH;

import java.io.InputStream;

import com.restify.http.client.Headers;

public abstract class BaseHttpResponseMessage implements HttpResponseMessage {

	private final StatusCode statusCode;
	private final Headers headers;
	private final InputStream body;

	protected BaseHttpResponseMessage(StatusCode statusCode, Headers headers, InputStream body) {
		this.statusCode = statusCode;
		this.headers = headers;
		this.body = body;
	}
	
	@Override
	public StatusCode code() {
		return statusCode;
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
	public boolean isReadable() {
		return isReadableStatus() && hasContentLength();
	}
	
	private boolean isReadableStatus() {
		return !(statusCode.isInformational() || statusCode.isNoContent() || statusCode.isNotModified());
	}

	private boolean hasContentLength() {
		int contentLength = headers.get(CONTENT_LENGTH).map(h -> Integer.valueOf(h.value())).orElse(-1);
		return contentLength != 0;
	}
}
