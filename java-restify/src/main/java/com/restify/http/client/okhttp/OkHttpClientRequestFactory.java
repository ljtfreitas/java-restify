package com.restify.http.client.okhttp;

import com.restify.http.client.EndpointRequest;
import com.restify.http.client.HttpClientRequestFactory;

import okhttp3.OkHttpClient;

public class OkHttpClientRequestFactory implements HttpClientRequestFactory {

	private final OkHttpClient okHttpClient;
	private final String charset;

	public OkHttpClientRequestFactory() {
		this(new OkHttpClient());
	}

	public OkHttpClientRequestFactory(OkHttpClient okHttpClient) {
		this(okHttpClient, "UTF-8");
	}

	public OkHttpClientRequestFactory(OkHttpClient okHttpClient, String charset) {
		this.okHttpClient = okHttpClient;
		this.charset = charset;
	}

	@Override
	public OkHttpClientRequest createOf(EndpointRequest endpointRequest) {
		return new OkHttpClientRequest(okHttpClient, endpointRequest, charset);
	}

}
