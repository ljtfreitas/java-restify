package com.github.ljtfreitas.restify.http.client.request;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.charset.Encoding;

public class SimpleHttpRequestMessage implements HttpRequestMessage {

	private final OutputStream output;
	private final Headers headers;
	private final EndpointRequest source;
	private final Encoding encoding;

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

	public SimpleHttpRequestMessage(OutputStream output, Encoding encoding) {
		this(null, output, new Headers(), encoding);
	}

	private SimpleHttpRequestMessage(EndpointRequest source, OutputStream output, Headers headers) {
		this(source, output, headers, Encoding.UTF_8);
	}

	private SimpleHttpRequestMessage(EndpointRequest source, OutputStream output, Headers headers, Encoding encoding) {
		this.source = source;
		this.output = output;
		this.headers = headers;
		this.encoding = encoding;
	}

	@Override
	public URI uri() {
		return source.endpoint();
	}

	@Override
	public String method() {
		return source.method();
	}

	@Override
	public OutputStream output() {
		return output;
	}

	@Override
	public Charset charset() {
		return encoding.charset();
	}

	@Override
	public Headers headers() {
		return headers;
	}

	public static SimpleHttpRequestMessage some() {
		return new SimpleHttpRequestMessage(new EndpointRequest(URI.create("http://some.api"), "GET"));
	}
}
