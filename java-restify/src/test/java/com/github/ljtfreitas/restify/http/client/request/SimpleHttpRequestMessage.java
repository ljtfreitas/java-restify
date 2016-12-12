package com.github.ljtfreitas.restify.http.client.request;

import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.charset.Encoding;

public class SimpleHttpRequestMessage implements HttpRequestMessage {

	private final OutputStream output;
	private final Headers headers;
	private final EndpointRequest source;

	public SimpleHttpRequestMessage(EndpointRequest source) {
		this(source, new ByteArrayOutputStream(), source.headers());
	}

	public SimpleHttpRequestMessage(Headers headers) {
		this(null, new ByteArrayOutputStream(), headers);
	}

	public SimpleHttpRequestMessage(OutputStream output) {
		this(output, new Headers());
	}

	public SimpleHttpRequestMessage(OutputStream output, Headers headers) {
		this(null, output, headers);
	}

	private SimpleHttpRequestMessage(EndpointRequest source, OutputStream output, Headers headers) {
		this.source = source;
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

	@Override
	public EndpointRequest source() {
		return source;
	}
}