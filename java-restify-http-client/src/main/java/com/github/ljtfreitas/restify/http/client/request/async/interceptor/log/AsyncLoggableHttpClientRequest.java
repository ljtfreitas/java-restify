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
package com.github.ljtfreitas.restify.http.client.request.async.interceptor.log;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.ByteArrayHttpResponseBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseBody;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;

class AsyncLoggableHttpClientRequest implements AsyncHttpClientRequest {

	private static final Logger log = Logger.getLogger(AsyncLoggableHttpClientRequest.class.getCanonicalName());

	private final AsyncHttpClientRequest source;
	private final AsyncLoggableHttpRequestBody body;

	AsyncLoggableHttpClientRequest(AsyncHttpClientRequest source) {
		this.source = source;
		this.body = new AsyncLoggableHttpRequestBody(source.body());
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
	public AsyncLoggableHttpRequestBody body() {
		return body;
	}

	@Override
	public Charset charset() {
		return source.charset();
	}

	@Override
	public HttpRequestMessage replace(Header header) {
		return new AsyncLoggableHttpClientRequest((AsyncHttpClientRequest) source.replace(header));
	}

	@Override
	public Headers headers() {
		return source.headers();
	}

	@Override
	public CompletionStage<HttpClientResponse> executeAsync() throws HttpClientException {
		log.info(requestAsLog());

		CompletionStage<HttpClientResponse> responseAsFuture = source.executeAsync();

		return responseAsFuture.thenApplyAsync(response -> {

			LoggableHttpClientResponse loggableHttpClientResponse = new LoggableHttpClientResponse(response);

			log.info(responseAsLog(loggableHttpClientResponse));

			return loggableHttpClientResponse;
		});
	}

	private String requestAsLog() {
		StringBuilder message = new StringBuilder();

		message.append("HTTP Request: ")
					.append("\n")
			   .append(source.method() + " " + source.uri())
					.append("\n")
			   .append("Headers: ")
					.append(source.headers())
					.append("\n")
			   .append("Body: ")
					.append(new String(body.asBytes(), source.charset()))
					.append("\n")
			   .append("-------------------------");

		return message.toString();
	}

	private String responseAsLog(LoggableHttpClientResponse response) {
		StringBuilder message = new StringBuilder();

		message.append("HTTP Response: ")
					.append("\n")
			   .append("Status code: ")
					.append(response.status())
					.append("\n")
			   .append("Headers: ")
					.append(response.headers())
					.append("\n")
			   .append("Body: ")
					.append(response.body.asString())
					.append("\n")
			   .append("-------------------------");

		return message.toString();
	}

	private class LoggableHttpClientResponse implements HttpClientResponse {

		private final HttpClientResponse source;
		private final ByteArrayHttpResponseBody body;

		private LoggableHttpClientResponse(HttpClientResponse source) {
			this.source = source;
			this.body = ByteArrayHttpResponseBody.of(source.body());
		}

		@Override
		public StatusCode status() {
			return source.status();
		}

		@Override
		public HttpResponseBody body() {
			return body;
		}

		@Override
		public boolean available() {
			return source.available();
		}

		@Override
		public HttpRequestMessage request() {
			return source.request();
		}

		@Override
		public Headers headers() {
			return source.headers();
		}

		@Override
		public void close() throws IOException {
			source.close();
		}
	}
}
