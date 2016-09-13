package com.restify.http.client.converter;

import java.io.InputStream;

import com.restify.http.client.EndpointResponseCode;
import com.restify.http.client.Headers;
import com.restify.http.client.HttpResponseMessage;

public class SimpleHttpResponseMessage implements HttpResponseMessage {

	private final InputStream input;

	public SimpleHttpResponseMessage(InputStream input) {
		this.input = input;
	}

	@Override
	public InputStream body() {
		return input;
	}

	@Override
	public EndpointResponseCode code() {
		return null;
	}

	@Override
	public Headers headers() {
		return null;
	}
}
