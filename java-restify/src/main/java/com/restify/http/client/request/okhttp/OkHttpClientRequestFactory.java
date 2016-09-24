package com.restify.http.client.request.okhttp;

import java.nio.charset.Charset;

import com.restify.http.client.charset.Encoding;
import com.restify.http.client.request.EndpointRequest;
import com.restify.http.client.request.HttpClientRequestFactory;

import okhttp3.OkHttpClient;

public class OkHttpClientRequestFactory implements HttpClientRequestFactory {

	private final OkHttpClient okHttpClient;
	private final Charset charset;

	public OkHttpClientRequestFactory() {
		this(new OkHttpClient());
	}

	public OkHttpClientRequestFactory(OkHttpClient okHttpClient) {
		this(okHttpClient, Encoding.UTF_8.charset());
	}

	public OkHttpClientRequestFactory(OkHttpClient okHttpClient, Charset charset) {
		this.okHttpClient = okHttpClient;
		this.charset = charset;
	}

	@Override
	public OkHttpClientRequest createOf(EndpointRequest endpointRequest) {
		return new OkHttpClientRequest(okHttpClient, endpointRequest, charset);
	}

}
