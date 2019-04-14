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
package com.github.ljtfreitas.restify.http.client.request.grizzly;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.message.request.BufferedByteArrayHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.BufferedHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.Timeout;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;
import com.github.ljtfreitas.restify.util.Try;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import com.ning.http.client.generators.ByteArrayBodyGenerator;

class GrizzlyHttpClientRequest implements AsyncHttpClientRequest {

	private final AsyncHttpClient asyncHttpClient;
	private final EndpointRequest source;
	private final Headers headers;
	private final Charset charset;
	private final BufferedHttpRequestBody body;

	GrizzlyHttpClientRequest(AsyncHttpClient asyncHttpClient, EndpointRequest source, Charset charset) {
		this(asyncHttpClient, source, new Headers(source.headers()), charset, new BufferedByteArrayHttpRequestBody(charset));
	}

	private GrizzlyHttpClientRequest(AsyncHttpClient asyncHttpClient, EndpointRequest source, Headers headers,
			Charset charset, BufferedHttpRequestBody body) {
		this.asyncHttpClient = asyncHttpClient;
		this.source = source;
		this.headers = headers;
		this.charset = charset;
		this.body = body;
	}

	@Override
	public URI uri() {
		return source.endpoint();
	}

	@Override
	public String method() {
		return source.method();
	}

	@Override
	public HttpRequestBody body() {
		return body;
	}

	@Override
	public Charset charset() {
		return charset;
	}

	@Override
	public HttpRequestMessage replace(Header header) {
		return new GrizzlyHttpClientRequest(asyncHttpClient, source, headers.replace(header), charset, body);
	}

	@Override
	public Headers headers() {
		return headers;
	}

	@Override
	public CompletionStage<HttpClientResponse> executeAsync() throws HttpClientException {
		CompletableFuture<HttpClientResponse> future = new CompletableFuture<>();

		Request request = buildRequest();

		asyncHttpClient.prepareRequest(request)
			.execute(new AsyncCompletionHandler<HttpClientResponse>() {

				@Override
				public HttpClientResponse onCompleted(Response response) throws Exception {
					HttpClientResponse httpClientResponse = read(response);

					future.complete(httpClientResponse);

					return httpClientResponse;
				}

				private HttpClientResponse read(Response response) {
					StatusCode statusCode = StatusCode.of(response.getStatusCode(), response.getStatusText());

					Headers responseHeaders = response.getHeaders().entrySet().stream()
							.reduce(new Headers(), (h, e) -> h.add(e.getKey(), e.getValue()), (a, b) -> b);

					InputStream body = Try.of(response::getResponseBodyAsStream)
							.error(HttpMessageReadException::new)
							.get();

					return new GrizzlyHttpClientResponse(statusCode, responseHeaders, body, GrizzlyHttpClientRequest.this);
				}

				@Override
				public void onThrowable(Throwable t) {
					future.completeExceptionally(new HttpClientException("I/O error on HTTP request: [" + request.getMethod() + " " +
							request.getUrl() + "]", t));
				}
			});

		return future;
	}

	private Request buildRequest() {
		RequestBuilder builder = new RequestBuilder()
				.setMethod(source.method())
				.setUrl(source.endpoint().toString());
		
		byte[] bodyAsBytes = body.asBytes();
		if (bodyAsBytes.length != 0) {
			builder.setBody(new ByteArrayBodyGenerator(bodyAsBytes));
		}
		
		source.metadata().get(Timeout.class).ifPresent(timeout -> {
			builder.setRequestTimeout((int) timeout.read());
		});

		headers.forEach(header -> builder.addHeader(header.name(), header.value()));

		return builder.build();
	}
}
