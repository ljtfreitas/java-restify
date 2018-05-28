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
package com.github.ljtfreitas.restify.http.client.request.jersey;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import javax.ws.rs.core.Response;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.BaseHttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;

class ErrorHttpResponseMessage extends BaseHttpResponseMessage {

	private ErrorHttpResponseMessage(StatusCode statusCode, Headers headers, InputStream body, HttpRequestMessage httpRequest) {
		super(statusCode, headers, body, httpRequest);
	}

	@Override
	public void close() throws IOException {
	}

	public static ErrorHttpResponseMessage from(Response response, EndpointRequest request) {
		StatusCode statusCode = StatusCode.of(response.getStatus(), response.getStatusInfo().getReasonPhrase());

		Headers headers = new Headers();
		response.getHeaders().forEach((key, values) -> values.forEach(value -> headers.add(new Header(key, value.toString()))));

		String bodyAsString = response.readEntity(String.class);

		InputStream body = new ByteArrayInputStream(bodyAsString.getBytes());

		return new ErrorHttpResponseMessage(statusCode, headers, body, new ErrorHttpRequestMessage(request));
	}

	private static class ErrorHttpRequestMessage implements HttpRequestMessage {

		private final EndpointRequest request;

		private ErrorHttpRequestMessage(EndpointRequest request) {
			this.request = request;
		}

		@Override
		public URI uri() {
			return request.endpoint();
		}

		@Override
		public String method() {
			return request.method();
		}

		@Override
		public RequestBody body() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Headers headers() {
			return request.headers();
		}

		@Override
		public Charset charset() {
			throw new UnsupportedOperationException();
		}

		@Override
		public HttpRequestMessage replace(Header header) {
			throw new UnsupportedOperationException("Headers are unmodifiable.");
		}
	}
}
