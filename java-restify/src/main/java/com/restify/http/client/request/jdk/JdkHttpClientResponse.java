package com.restify.http.client.request.jdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.restify.http.client.Headers;
import com.restify.http.client.response.BaseHttpResponseMessage;
import com.restify.http.client.response.StatusCode;

class JdkHttpClientResponse extends BaseHttpResponseMessage {

	private final HttpURLConnection connection;

	JdkHttpClientResponse(StatusCode statusCode, Headers headers, InputStream body, HttpURLConnection connection) {
		super(statusCode, headers, body);
		this.connection = connection;
	}

	@Override
	public void close() throws IOException {
		connection.disconnect();
	}

}
