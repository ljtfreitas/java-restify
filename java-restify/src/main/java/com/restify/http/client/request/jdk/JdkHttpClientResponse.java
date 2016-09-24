package com.restify.http.client.request.jdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.restify.http.client.Headers;
import com.restify.http.client.response.BaseHttpResponseMessage;
import com.restify.http.client.response.EndpointResponseCode;

class JdkHttpClientResponse extends BaseHttpResponseMessage {

	private final HttpURLConnection connection;

	JdkHttpClientResponse(EndpointResponseCode code, Headers headers, InputStream body, HttpURLConnection connection) {
		super(code, headers, body);
		this.connection = connection;
	}

	@Override
	public void close() throws IOException {
		connection.disconnect();
	}

}
