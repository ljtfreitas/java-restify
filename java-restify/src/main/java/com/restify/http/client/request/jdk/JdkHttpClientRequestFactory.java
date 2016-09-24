package com.restify.http.client.request.jdk;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.charset.Encoding;
import com.restify.http.client.request.EndpointRequest;
import com.restify.http.client.request.HttpClientRequestFactory;

public class JdkHttpClientRequestFactory implements HttpClientRequestFactory {

	private final Charset charset;

	public JdkHttpClientRequestFactory() {
		this(Encoding.UTF_8.charset());
	}

	public JdkHttpClientRequestFactory(Charset charset) {
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
