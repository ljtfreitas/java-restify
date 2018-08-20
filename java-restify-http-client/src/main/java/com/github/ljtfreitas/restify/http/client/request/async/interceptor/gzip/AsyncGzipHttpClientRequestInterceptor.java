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
package com.github.ljtfreitas.restify.http.client.request.async.interceptor.gzip;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.async.interceptor.AsyncHttpClientRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.gzip.GzipHttpClientRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.gzip.GzipHttpClientResponse;
import com.github.ljtfreitas.restify.http.client.request.interceptor.gzip.GzipHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;

public class AsyncGzipHttpClientRequestInterceptor implements AsyncHttpClientRequestInterceptor {

	private static final String GZIP_ALGORITHM = "gzip";

	private final boolean applyToRequest;
	private final boolean applyToResponse;
	private final GzipHttpClientRequestInterceptor delegate;

	public AsyncGzipHttpClientRequestInterceptor() {
		this(false, true);
	}

	private AsyncGzipHttpClientRequestInterceptor(boolean applyToRequest, boolean applyToResponse) {
		this.applyToRequest = applyToRequest;
		this.applyToResponse = applyToResponse;
		this.delegate = new GzipHttpClientRequestInterceptor.Builder()
				.encoding()
					.request(applyToRequest)
					.response(applyToResponse)
					.build();
	}

	@Override
	public HttpClientRequest intercepts(HttpClientRequest request) {
		return delegate.intercepts(request);
	}

	@Override
	public AsyncHttpClientRequest interceptsAsync(AsyncHttpClientRequest request) {
		return new AsyncGzipHttpClientRequest(request);
	}

	private class AsyncGzipHttpClientRequest implements AsyncHttpClientRequest {

		private final AsyncHttpClientRequest source;
		private final HttpRequestBody body;

		private AsyncGzipHttpClientRequest(AsyncHttpClientRequest source) {
			this.source = source;
			this.body = applyToRequest ? new GzipHttpRequestBody(source.body()) : source.body();
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
			return body;
		}

		@Override
		public Charset charset() {
			return source.charset();
		}

		@Override
		public HttpRequestMessage replace(Header header) {
			return source.replace(header);
		}

		@Override
		public Headers headers() {
			return source.headers();
		}

		@Override
		public HttpClientResponse execute() throws HttpClientException {
			return source.execute();
		}

		@Override
		public CompletionStage<HttpClientResponse> executeAsync() throws HttpClientException {
			CompletionStage<HttpClientResponse> responseAsFuture = applyToRequest ?
					withGzip(source).executeAsync() :
						source.executeAsync();

			if (applyToResponse) {
				return responseAsFuture.thenApply(response -> {
					String encoding = response.headers().all(Headers.CONTENT_ENCODING)
							.stream()
							.map(Header::value)
							.collect(Collectors.joining(","));

					return encoding.contains(GZIP_ALGORITHM) ? new GzipHttpClientResponse(response) : response;
				});

			} else return responseAsFuture;
		}

		private AsyncHttpClientRequest withGzip(AsyncHttpClientRequest request) {
			if (!body.empty()) {
				return request.headers().get(Headers.CONTENT_ENCODING)
					.map(h -> request)
						.orElseGet(() -> (AsyncHttpClientRequest) request.replace(Header.contentEncoding(GZIP_ALGORITHM)));

			} else return request;
		}
	}

	public static class Builder {

		private final AsyncGzipHttpClientEncodingBuilder encoding = new AsyncGzipHttpClientEncodingBuilder();

		public AsyncGzipHttpClientEncodingBuilder encoding() {
			return encoding;
		}

		public AsyncGzipHttpClientRequestInterceptor build() {
			return new AsyncGzipHttpClientRequestInterceptor(encoding.request, encoding.response);
		}

		public class AsyncGzipHttpClientEncodingBuilder {

			private boolean request = false;
			private boolean response = true;

			public AsyncGzipHttpClientEncodingBuilder request() {
				this.request = true;
				return this;
			}

			public AsyncGzipHttpClientEncodingBuilder request(boolean enabled) {
				this.request = enabled;
				return this;
			}

			public AsyncGzipHttpClientEncodingBuilder response() {
				this.response = true;
				return this;
			}

			public AsyncGzipHttpClientEncodingBuilder response(boolean enabled) {
				this.response = enabled;
				return this;
			}

			public Builder both() {
				this.request = true;
				this.response = true;
				return Builder.this;
			}

			public AsyncGzipHttpClientRequestInterceptor build() {
				return Builder.this.build();
			}
		}
	}
}
