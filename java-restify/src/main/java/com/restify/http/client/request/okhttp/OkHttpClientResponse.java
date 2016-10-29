package com.restify.http.client.request.okhttp;

import java.io.IOException;
import java.io.InputStream;

import com.restify.http.client.Headers;
import com.restify.http.client.response.BaseHttpResponseMessage;
import com.restify.http.client.response.StatusCode;

import okhttp3.Response;

class OkHttpClientResponse extends BaseHttpResponseMessage {

	private final Response response;

	OkHttpClientResponse(StatusCode statusCode, Headers headers, InputStream body, Response response) {
		super(statusCode, headers, body);
		this.response = response;
	}

	@Override
	public void close() throws IOException {
		response.close();
	}
}
