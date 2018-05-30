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
package com.github.ljtfreitas.restify.http.client.request.interceptor.log;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.interceptor.HttpClientRequestInterceptor;
import com.github.ljtfreitas.restify.util.Tryable;

public class LogHttpClientRequestInterceptor implements HttpClientRequestInterceptor {

	private static final Logger log = Logger.getLogger(LogHttpClientRequestInterceptor.class.getCanonicalName());

	@Override
	public HttpClientRequest intercepts(HttpClientRequest request) {
		return new LogHttpClientRequest(request);
	}

	private class LogHttpClientRequest implements HttpClientRequest {

		private final HttpClientRequest source;

		private LogHttpClientRequest(HttpClientRequest source) {
			this.source = source;
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
		public RequestBody body() {
			return source.body();
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
		public HttpResponseMessage execute() throws HttpClientException {
			if (log.isLoggable(Level.INFO)) {
				log.log(record());
			}

			return source.execute();
		}

		private LogRecord record() {
			StringBuilder message = new StringBuilder();

			message.append("**********")
				   .append("\n")
				   .append("> " + source.method() + " " + source.uri());

			source.headers().forEach(h -> message.append("\n").append("> " + h.toString()));

			if (!source.body().empty()) {
				message.append("\n")
					   .append("> " + Tryable.of(() -> source.body().toString(source.charset().name())))
					   .append("\n");
			}

			message.append(">");

			return new LogRecord(Level.INFO, message.toString());
		}
	}
}
