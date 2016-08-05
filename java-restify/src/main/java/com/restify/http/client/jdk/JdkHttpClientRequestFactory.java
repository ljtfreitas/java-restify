package com.restify.http.client.jdk;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.EndpointRequest;
import com.restify.http.client.HttpClientRequestFactory;

public class JdkHttpClientRequestFactory implements HttpClientRequestFactory {

	private final String charset;

	public JdkHttpClientRequestFactory() {
		this("UTF-8");
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

			request.headers().all().forEach(h -> connection.setRequestProperty(h.name(), h.value()));

			return new JdkHttpClientRequest(connection, charset);

		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}
	}

}
