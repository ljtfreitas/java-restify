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
package com.github.ljtfreitas.restify.http.client.request.vertx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.response.BaseHttpClientResponse;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;

class VertxHttpClientResponse extends BaseHttpClientResponse {

	private final InputStream body;

	VertxHttpClientResponse(StatusCode status, Headers headers, InputStream body, HttpRequestMessage httpRequest) {
		super(status, headers, body, httpRequest);
		this.body = body;
	}

	@Override
	public void close() throws IOException {
		body.close();
	}

	static VertxHttpClientResponse read(HttpResponse<Buffer> result, HttpRequestMessage source) {
		InputStream body = Optional.ofNullable(result.body())
				.map(Buffer::getBytes)
				.map(ByteArrayInputStream::new)
				.orElse(null);

		Headers headers = result.headers().entries().stream()
				.reduce(new Headers(), (a, h) -> a.add(h.getKey(), h.getValue()) , (a, b) -> b);

		StatusCode status = StatusCode.of(result.statusCode(), result.statusMessage());

		return new VertxHttpClientResponse(status, headers, body, source);
	}
}
