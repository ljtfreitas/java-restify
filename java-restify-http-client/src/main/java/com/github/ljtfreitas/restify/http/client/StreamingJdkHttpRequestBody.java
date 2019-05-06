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
package com.github.ljtfreitas.restify.http.client;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.function.Supplier;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.SimpleOutputStreamHttpRequestBody;
import com.github.ljtfreitas.restify.util.Memoized;
import com.github.ljtfreitas.restify.util.Try;

class StreamingJdkHttpRequestBody implements HttpRequestBody {

	private final Memoized<HttpRequestBody> memoized;

	private StreamingJdkHttpRequestBody(Supplier<HttpRequestBody> supplier) {
		this.memoized = Memoized.of(supplier);
	}

	@Override
	public OutputStream output() {
		return memoized.get().output();
	}

	static StreamingJdkHttpRequestBody create(HttpURLConnection connection, Headers headers, HttpClientRequestConfiguration configuration) {
		return new StreamingJdkHttpRequestBody(() ->
			Try.of(() -> {
				headers.forEach(h -> connection.setRequestProperty(h.name(), h.value()));

				if (configuration.outputStreaming()) {
					long contentLength = headers.get(Headers.CONTENT_LENGTH)
							.map(Header::value)
							.map(Long::valueOf)
							.orElse(-1l);

					if (contentLength >= 0) {
						connection.setFixedLengthStreamingMode(contentLength);
					} else {
						connection.setChunkedStreamingMode(configuration.chunkSize());
					}
				}

				return new SimpleOutputStreamHttpRequestBody(connection.getOutputStream());
			})
			.error(e -> new IllegalStateException("Cannot create a streamed HttpResponseBody", e))
			.get()
		);
	}
}
