package com.github.ljtfreitas.restify.http.client.message.converter.form;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.ljtfreitas.restify.http.client.header.Headers;
import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.response.BaseHttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;

public class SimpleHttpResponseMessage implements HttpResponseMessage {

	private final HttpResponseMessage delegate;

	public SimpleHttpResponseMessage() {
		this(StatusCode.ok(), new Headers(), new ByteArrayInputStream(new byte[0]));
	}

	public SimpleHttpResponseMessage(InputStream input) {
		this(input, null);
	}

	public SimpleHttpResponseMessage(InputStream input, HttpRequestMessage source) {
		this(StatusCode.ok(), new Headers(), input, source);
	}

	public SimpleHttpResponseMessage(InputStream input, HttpRequestMessage source, Headers headers) {
		this(StatusCode.ok(), headers, input, source);
	}

	public SimpleHttpResponseMessage(StatusCode statusCode) {
		this(statusCode, null);
	}

	public SimpleHttpResponseMessage(StatusCode statusCode, HttpRequestMessage source) {
		this(statusCode, new Headers(), new ByteArrayInputStream(new byte[0]), source);
	}

	public SimpleHttpResponseMessage(Headers headers) {
		this(StatusCode.ok(), headers, new ByteArrayInputStream(new byte[0]));
	}

	public SimpleHttpResponseMessage(StatusCode code, Headers headers, InputStream input) {
		this(code, headers, input, SimpleHttpRequestMessage.some());
	}

	public SimpleHttpResponseMessage(StatusCode code, Headers headers, InputStream input, HttpRequestMessage source) {
		this.delegate = new BaseHttpResponseMessage(code, headers, input, source) {
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
	public StatusCode status() {
		return delegate.status();
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

	@Override
	public HttpRequestMessage request() {
		return delegate.request();
	}
}
