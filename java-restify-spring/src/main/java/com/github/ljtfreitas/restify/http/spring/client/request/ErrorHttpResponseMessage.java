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
package com.github.ljtfreitas.restify.http.spring.client.request;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestClientResponseException;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.BaseHttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;

public class ErrorHttpResponseMessage extends BaseHttpResponseMessage {

	private ErrorHttpResponseMessage(StatusCode statusCode, Headers headers, InputStream body, HttpRequestMessage httpRequest) {
		super(statusCode, headers, body, httpRequest);
	}

	@Override
	public void close() throws IOException {
	}

	public static ErrorHttpResponseMessage from(RequestEntity<Object> request, RestClientResponseException e) {
		StatusCode statusCode = StatusCode.of(e.getRawStatusCode(), e.getStatusText());

		Headers headers = new Headers();
		e.getResponseHeaders().forEach((k, v) -> headers.add(k, v));

		InputStream body = new ByteArrayInputStream(e.getResponseBodyAsByteArray());

		return new ErrorHttpResponseMessage(statusCode, headers, body, new ErrorHttpRequestMessage(request));
	}

	private static class ErrorHttpRequestMessage implements HttpRequestMessage {

		private final RequestEntity<Object> request;
		private final Headers headers;

		private ErrorHttpRequestMessage(RequestEntity<Object> request) {
			this.request = request;

			Headers headers = new Headers();
			request.getHeaders().forEach((k, v) -> headers.add(k, v));
			this.headers = headers;
		}

		@Override
		public URI uri() {
			return request.getUrl();
		}

		@Override
		public String method() {
			return request.getMethod().name();
		}

		@Override
		public RequestBody body() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Headers headers() {
			return headers;
		}

		@Override
		public Charset charset() {
			throw new UnsupportedOperationException();
		}

		@Override
		public HttpRequestMessage replace(Header header) {
			throw new UnsupportedOperationException();
		}
	}
}
