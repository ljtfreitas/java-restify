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
package com.github.ljtfreitas.restify.http.client.jdk;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.util.Try;

abstract class BaseJdkHttpClientRequest implements HttpClientRequest {

	private final HttpURLConnection connection;
	private final Charset charset;
	private final Headers headers;
	private final HttpRequestBody body;
	private final HttpClientRequestConfiguration configuration;

	BaseJdkHttpClientRequest(HttpURLConnection connection, Charset charset, Headers headers, HttpRequestBody body, HttpClientRequestConfiguration configuration) {
		this.connection = connection;
		this.charset = charset;
		this.headers = headers;
		this.body = body;
		this.configuration = configuration;
	}

	protected JdkHttpClientResponse responseOf(HttpURLConnection connection) throws IOException {
		StatusCode status = StatusCode.of(connection.getResponseCode(), connection.getResponseMessage());

		Headers headers = connection.getHeaderFields().entrySet().stream()
			.filter(e -> e.getKey() != null && !e.getKey().equals("") && !e.getValue().isEmpty())
				.reduce(new Headers(),
					(a, b) -> a.add(new Header(b.getKey(), b.getValue().stream().findFirst().get())),
						(a, b) -> b);

		InputStream stream = Try.of(() -> connection.getErrorStream() == null ? connection.getInputStream() : connection.getErrorStream())
				.or(() -> new ByteArrayInputStream(new byte[0]));

		return new JdkHttpClientResponse(status, headers, stream, connection, this);
	}

	@Override
	public URI uri() {
		return Try.of(() -> connection.getURL()).map(URL::toURI).get();
	}

	@Override
	public String method() {
		return connection.getRequestMethod();
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
	public Headers headers() {
		return headers;
	}

	@Override
	public HttpRequestMessage replace(Header header) {
		return doReplace(connection, charset, headers.replace(header), body, configuration);
	}

	abstract HttpRequestMessage doReplace(HttpURLConnection connection, Charset charset, Headers newHeaders,
			HttpRequestBody body, HttpClientRequestConfiguration configuration);
}
