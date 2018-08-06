package com.github.ljtfreitas.restify.http.client.request.async;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;

public class AsyncHttpClientRequestFactoryAdapter implements AsyncHttpClientRequestFactory {

	private final Executor executor;
	private final HttpClientRequestFactory delegate;

	public AsyncHttpClientRequestFactoryAdapter(Executor executor, HttpClientRequestFactory delegate) {
		this.executor = executor;
		this.delegate = delegate;
	}

	@Override
	public HttpClientRequest createOf(EndpointRequest endpointRequest) {
		return delegate.createOf(endpointRequest);
	}

	@Override
	public AsyncHttpClientRequest createAsyncOf(EndpointRequest endpointRequest) {
		return new AsyncHttpClientRequestAdapter(delegate.createOf(endpointRequest));
	}

	private class AsyncHttpClientRequestAdapter implements AsyncHttpClientRequest {

		private final HttpClientRequest source;

		private AsyncHttpClientRequestAdapter(HttpClientRequest delegate) {
			this.source = delegate;
		}

		@Override
		public HttpClientResponse execute() throws HttpClientException {
			return source.execute();
		}

		@Override
		public URI uri() {
			return source.uri();
		}

		@Override
		public String method() {
			return source.method();
		}

		@Override
		public HttpRequestBody body() {
			return source.body();
		}

		@Override
		public Charset charset() {
			return source.charset();
		}

		@Override
		public HttpRequestMessage replace(Header header) {
			return new AsyncHttpClientRequestAdapter((HttpClientRequest) source.replace(header));
		}

		@Override
		public Headers headers() {
			return source.headers();
		}

		@Override
		public CompletionStage<HttpClientResponse> executeAsync() throws HttpClientException {
			return CompletableFuture.supplyAsync(source::execute, executor);
		}
	}
}
