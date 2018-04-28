/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client.apache.httpclient;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.protocol.HttpContext;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.util.Tryable;

class ApacheAsyncHttpClientRequest implements AsyncHttpClientRequest {

	private final HttpAsyncClient httpAsyncClient;
	private final HttpUriRequest httpRequest;
	private final HttpContext httpContext;
	private final Charset charset;
	private final Headers headers;

	private final BufferedEntity bufferedEntity;

	ApacheAsyncHttpClientRequest(HttpAsyncClient httpAsyncClient, HttpUriRequest httpRequest, HttpContext context,
			Charset charset, Headers headers) {
		this(httpAsyncClient, httpRequest, context, charset, headers, new BufferedEntity());
	}

	private ApacheAsyncHttpClientRequest(HttpAsyncClient httpAsyncClient, HttpUriRequest httpRequest,
			HttpContext context, Charset charset, Headers headers, BufferedEntity bufferedEntity) {
		this.httpAsyncClient = httpAsyncClient;
		this.httpRequest = httpRequest;
		this.httpContext = context;
		this.charset = charset;
		this.headers = headers;
		this.bufferedEntity = bufferedEntity;
	}

	@Override
	public URI uri() {
		return httpRequest.getURI();
	}

	@Override
	public String method() {
		return httpRequest.getMethod();
	}

	@Override
	public OutputStream output() {
		return bufferedEntity.output();
	}

	@Override
	public Charset charset() {
		return charset;
	}

	@Override
	public HttpRequestMessage replace(Header header) {
		return new ApacheAsyncHttpClientRequest(httpAsyncClient, httpRequest, httpContext, charset,
				headers.replace(header), bufferedEntity);
	}

	@Override
	public Headers headers() {
		return headers;
	}

	@Override
	public CompletableFuture<HttpResponseMessage> executeAsync() throws HttpClientException {
		return doExecuteAsync();
	}

	private CompletableFuture<HttpResponseMessage> doExecuteAsync() {
		headers.all().forEach(h -> httpRequest.addHeader(h.name(), h.value()));

		start();

		if (httpRequest instanceof HttpEntityEnclosingRequest) {
			HttpEntityEnclosingRequest entityEnclosingRequest = (HttpEntityEnclosingRequest) httpRequest;
			HttpEntity requestEntity = new ByteArrayEntity(bufferedEntity.asByteArray());
			entityEnclosingRequest.setEntity(requestEntity);
		}

		CompletableFuture<HttpResponseMessage> future = new CompletableFuture<>();

		httpAsyncClient.execute(httpRequest, httpContext, new ApacheHttpAsyncRequestCallback(future));

		return future;
	}

	private void start() {
		if (httpAsyncClient instanceof CloseableHttpAsyncClient) {
			CloseableHttpAsyncClient closeableHttpAsyncClient = (CloseableHttpAsyncClient) httpAsyncClient;

			if (!closeableHttpAsyncClient.isRunning()) {
				closeableHttpAsyncClient.start();
			}
		}
	}

	@Override
	public HttpResponseMessage execute() throws HttpClientException {
		return Tryable.of(() -> doExecuteAsync().get());
	}

	private class ApacheHttpAsyncRequestCallback implements FutureCallback<HttpResponse> {

		private final CompletableFuture<HttpResponseMessage> future;

		private ApacheHttpAsyncRequestCallback(CompletableFuture<HttpResponseMessage> future) {
			this.future = future;
		}

		@Override
		public void cancelled() {
		}

		@Override
		public void completed(HttpResponse httpResponse) {
			ApacheHttpResponseReader reader = new ApacheHttpResponseReader(httpResponse, ApacheAsyncHttpClientRequest.this);

			ApacheHttpClientResponse apacheHttpClientResponse = Tryable.of(reader::read, e -> new HttpClientException(e));

			future.complete(apacheHttpClientResponse);
		}

		@Override
		public void failed(Exception exception) {
			future.completeExceptionally(new HttpClientException("I/O error on HTTP request: [" + httpRequest.getMethod() + " " +
					httpRequest.getURI() + "]", exception));
		}
	}
}
