package com.restify.http.client.jdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.EndpointResponse;
import com.restify.http.client.EndpointResponseCode;
import com.restify.http.client.Headers;
import com.restify.http.client.HttpClientRequest;

public class JdkHttpClientRequest implements HttpClientRequest {

	private final HttpURLConnection connection;
	private final String charset;

	public JdkHttpClientRequest(HttpURLConnection connection, String charset) {
		this.connection = connection;
		this.charset = charset;
	}

	@Override
	public EndpointResponse execute() {
		try {
			connection.connect();

			EndpointResponse response = responseOf(connection);

			return response;

		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}
	}

	private EndpointResponse responseOf(HttpURLConnection connection) throws IOException {
		EndpointResponseCode code = new EndpointResponseCode(connection.getResponseCode());

		Headers headers = new Headers();

		connection.getHeaderFields().entrySet().stream()
			.filter(e -> e.getKey() != null && !e.getKey().equals(""))
				.forEach(e -> headers.put(e.getKey(), e.getValue()));

		InputStream stream = connection.getErrorStream() == null ? connection.getInputStream() : connection.getErrorStream();

		return new EndpointResponse(code, headers, stream) {

			@Override
			public void close() throws IOException {
				connection.disconnect();
			}
		};
	}

	@Override
	public OutputStream output() {
		try {
			return connection.getOutputStream();
		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}
	}

	@Override
	public String charset() {
		return charset;
	}
}
