package com.github.ljtfreitas.restify.http.client.response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.response.BaseHttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;

public class SimpleHttpResponseMessage implements HttpResponseMessage {

	private final HttpResponseMessage delegate;

	public SimpleHttpResponseMessage() {
		this(StatusCode.ok(), new Headers(), new ByteArrayInputStream(new byte[0]));
	}

	public SimpleHttpResponseMessage(InputStream input) {
		this(StatusCode.ok(), new Headers(), input);
	}

	public SimpleHttpResponseMessage(StatusCode statusCode) {
		this(statusCode, new Headers(), new ByteArrayInputStream(new byte[0]));
	}

	public SimpleHttpResponseMessage(Headers headers) {
		this(StatusCode.ok(), headers, new ByteArrayInputStream(new byte[0]));
	}

	public SimpleHttpResponseMessage(StatusCode code, Headers headers, InputStream input) {
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
	public StatusCode code() {
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
	public boolean isReadable() {
		return delegate.isReadable();
	}
}
