package com.restify.http.client.jdk;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.EndpointRequest;
import com.restify.http.client.HttpClientRequestFactory;
import com.restify.http.client.charset.Encoding;

public class JdkHttpClientRequestFactory implements HttpClientRequestFactory {

	private final String charset;

	public JdkHttpClientRequestFactory() {
		this(Encoding.UTF_8.charset().name());
	}

	public JdkHttpClientRequestFactory(String charset) {
		this.charset = charset;
	}

	@Override
	public JdkHttpClientRequest createOf(EndpointRequest request) {
		try {
			HttpURLConnection connection = (HttpURLConnection) request.endpoint().toURL().openConnection();

			connection.setDoOutput(true);
			connection.setRequestMethod(request.method());

			return new JdkHttpClientRequest(connection, charset, request.headers());

		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}
	}

}
