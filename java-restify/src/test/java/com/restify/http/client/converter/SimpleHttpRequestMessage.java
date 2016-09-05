package com.restify.http.client.converter;

import java.io.OutputStream;

import com.restify.http.client.Headers;
import com.restify.http.client.HttpRequestMessage;

public class SimpleHttpRequestMessage implements HttpRequestMessage {

	private final OutputStream output;
	private final Headers headers = new Headers();

	public SimpleHttpRequestMessage(OutputStream output) {
		this.output = output;
	}

	@Override
	public OutputStream output() {
		return output;
	}

	@Override
	public String charset() {
		return "UTF-8";
	}

	@Override
	public Headers headers() {
		return headers;
	}
}
