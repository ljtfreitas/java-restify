package com.restify.http.client.converter;

import java.io.OutputStream;

import com.restify.http.client.HttpRequestMessage;

public class SimpleHttpRequestMessage implements HttpRequestMessage {

	private final OutputStream output;

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
}
