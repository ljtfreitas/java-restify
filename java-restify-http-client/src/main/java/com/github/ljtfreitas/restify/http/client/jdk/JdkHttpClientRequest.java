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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.util.Tryable;

class JdkHttpClientRequest implements HttpClientRequest {

	private final HttpURLConnection connection;
	private final Charset charset;
	private final Headers headers;

	public JdkHttpClientRequest(HttpURLConnection connection, Charset charset, Headers headers) {
		this.connection = connection;
		this.charset = charset;
		this.headers = new JdkHttpClientHeadersDecorator(connection, headers);
	}

	@Override
	public HttpResponseMessage execute() throws HttpClientException {
		try {
			connection.connect();

			return responseOf(connection);

		} catch (IOException e) {
			throw new HttpClientException("I/O error on HTTP request: [" + connection.getRequestMethod() + " " +
					connection.getURL() + "]", e);
		}
	}

	private JdkHttpClientResponse responseOf(HttpURLConnection connection) throws IOException {
		StatusCode status = StatusCode.of(connection.getResponseCode(), connection.getResponseMessage());

		Headers headers = connection.getHeaderFields().entrySet().stream()
			.filter(e -> e.getKey() != null && !e.getKey().equals("") && e.getValue().size() >= 1)
				.reduce(new Headers(),
					(a, b) -> a.add(new Header(b.getKey(), b.getValue().stream().findFirst().get())),
						(a, b) -> b);

		InputStream stream = Tryable.or(() -> connection.getErrorStream() == null ? connection.getInputStream() : connection.getErrorStream(),
				new ByteArrayInputStream(new byte[0]));

		return new JdkHttpClientResponse(status, headers, stream, connection, this);
	}

	@Override
	public URI uri() {
		return Tryable.of(() -> connection.getURL().toURI());
	}

	@Override
	public String method() {
		return connection.getRequestMethod();
	}

	@Override
	public OutputStream output() {
		return Tryable.of(() -> connection.getOutputStream());
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
		return new JdkHttpClientRequest(connection, charset, headers.replace(header));
	}

	private class JdkHttpClientHeadersDecorator extends Headers {

		private final Headers headers;
		private final HttpURLConnection connection;

		public JdkHttpClientHeadersDecorator(HttpURLConnection connection, Headers headers) {
			super(headers);
			this.connection = connection;
			this.headers = headers;
			apply();
		}

		private void apply() {
			headers.all().forEach(h -> connection.setRequestProperty(h.name(), h.value()));
		}

		@Override
		public Headers add(String name, String value) {
			connection.setRequestProperty(name, value);
			return new JdkHttpClientHeadersDecorator(connection, super.add(name, value));
		}

		@Override
		public Headers add(String name, Collection<String> values) {
			values.forEach(value -> connection.setRequestProperty(name, value));
			return new JdkHttpClientHeadersDecorator(connection, super.add(name, values));
		}

		@Override
		public Headers replace(String name, String value) {
			connection.setRequestProperty(name, value);
			return new JdkHttpClientHeadersDecorator(connection, super.replace(name, value));
		}
		
		@Override
		public Headers replace(Header header) {
			connection.setRequestProperty(header.name(), header.value());
			return new JdkHttpClientHeadersDecorator(connection, super.replace(header));
		}
	}
}
