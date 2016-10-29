package com.restify.http.client.request.jdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Collection;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.Headers;
import com.restify.http.client.request.HttpClientRequest;
import com.restify.http.client.response.StatusCode;
import com.restify.http.client.response.HttpResponseMessage;

public class JdkHttpClientRequest implements HttpClientRequest {

	private final HttpURLConnection connection;
	private final Charset charset;
	private final Headers headers;

	public JdkHttpClientRequest(HttpURLConnection connection, Charset charset, Headers headers) {
		this.connection = connection;
		this.charset = charset;
		this.headers = new JdkHttpClientHeadersDecorator(connection, headers);
	}

	@Override
	public HttpResponseMessage execute() {
		try {
			connection.connect();

			return responseOf(connection);

		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}
	}

	private JdkHttpClientResponse responseOf(HttpURLConnection connection) throws IOException {
		StatusCode statusCode = StatusCode.of(connection.getResponseCode());

		Headers headers = new Headers();

		connection.getHeaderFields().entrySet().stream()
			.filter(e -> e.getKey() != null && !e.getKey().equals(""))
				.forEach(e -> headers.put(e.getKey(), e.getValue()));

		InputStream stream = connection.getErrorStream() == null ? connection.getInputStream() : connection.getErrorStream();

		return new JdkHttpClientResponse(statusCode, headers, stream, connection);
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
	public Charset charset() {
		return charset;
	}

	@Override
	public Headers headers() {
		return headers;
	}

	private class JdkHttpClientHeadersDecorator extends Headers {

		private final Headers headers;
		private final HttpURLConnection connection;

		public JdkHttpClientHeadersDecorator(HttpURLConnection connection, Headers headers) {
			super(headers);
			this.connection = connection;
			this.headers = headers;
			apply();
		}

		private void apply() {
			headers.all().forEach(h -> connection.setRequestProperty(h.name(), h.value()));
		}

		@Override
		public void put(String name, String value) {
			super.put(name, value);
			connection.setRequestProperty(name, value);
		}

		@Override
		public void put(String name, Collection<String> values) {
			super.put(name, values);
			values.forEach(value -> connection.setRequestProperty(name, value));
		}

		@Override
		public void replace(String name, String value) {
			super.replace(name, value);
			connection.setRequestProperty(name, value);
		}
	}
}
