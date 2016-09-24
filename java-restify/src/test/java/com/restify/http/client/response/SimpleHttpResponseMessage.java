package com.restify.http.client.response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.restify.http.client.Headers;
import com.restify.http.client.response.EndpointResponseCode;
import com.restify.http.client.response.HttpResponseMessage;

public class SimpleHttpResponseMessage implements HttpResponseMessage {

	private final HttpResponseMessage delegate;

	public SimpleHttpResponseMessage() {
		this(EndpointResponseCode.ok(), new Headers(), new ByteArrayInputStream(new byte[0]));
	}

	public SimpleHttpResponseMessage(InputStream input) {
		this(EndpointResponseCode.ok(), new Headers(), input);
	}

	public SimpleHttpResponseMessage(EndpointResponseCode code) {
		this(code, new Headers(), new ByteArrayInputStream(new byte[0]));
	}

	public SimpleHttpResponseMessage(Headers headers) {
		this(EndpointResponseCode.ok(), headers, new ByteArrayInputStream(new byte[0]));
	}

	public SimpleHttpResponseMessage(EndpointResponseCode code, Headers headers, InputStream input) {
		this.delegate = new BaseHttpResponseMessage(code, headers, input) {
			@Override
			public void close() throws IOException {
				input.close();
			}
		};
	}

	@Override
	public InputStream body() {
		return delegate.body();
	}

	@Override
	public EndpointResponseCode code() {
		return delegate.code();
	}

	@Override
	public Headers headers() {
		return delegate.headers();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public boolean readable() {
		return delegate.readable();
	}
}
