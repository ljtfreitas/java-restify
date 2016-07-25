package com.restify.http.client.converter;

import java.io.InputStream;

import com.restify.http.client.HttpResponseMessage;

public class SimpleHttpResponseMessage implements HttpResponseMessage {

	private final InputStream input;

	public SimpleHttpResponseMessage(InputStream input) {
		this.input = input;
	}

	@Override
	public InputStream input() {
		return input;
	}
}
