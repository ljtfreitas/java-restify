package com.github.ljtfreitas.restify.http.client.request;

import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.charset.Encoding;
import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;

public class SimpleHttpRequestMessage implements HttpRequestMessage {

	private final OutputStream output;
	private final Headers headers;

	public SimpleHttpRequestMessage() {
		this(new ByteArrayOutputStream(), new Headers());
	}

	public SimpleHttpRequestMessage(Headers headers) {
		this(new ByteArrayOutputStream(), headers);
	}

	public SimpleHttpRequestMessage(OutputStream output) {
		this(output, new Headers());
	}

	public SimpleHttpRequestMessage(OutputStream output, Headers headers) {
		this.output = output;
		this.headers = headers;
	}

	@Override
	public OutputStream output() {
		return output;
	}

	@Override
	public Charset charset() {
		return Encoding.UTF_8.charset();
	}

	@Override
	public Headers headers() {
		return headers;
	}
}
