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

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactory;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class VertxHttpClientRequestFactory implements AsyncHttpClientRequestFactory, Closeable {

	private static final Charset DEFAULT_VERTX_CHARSET = Encoding.UTF_8.charset();

	private final WebClient webClient;

	public VertxHttpClientRequestFactory() {
		this(Vertx.vertx());
	}

	public VertxHttpClientRequestFactory(Vertx vertx) {
		this(WebClient.create(vertx));
	}

	public VertxHttpClientRequestFactory(WebClientOptions webClientOptions) {
		this(Vertx.vertx(), webClientOptions);
	}

	public VertxHttpClientRequestFactory(Vertx vertx, WebClientOptions webClientOptions) {
		this(WebClient.create(vertx, webClientOptions));
	}

	public VertxHttpClientRequestFactory(WebClient webClient) {
		this.webClient = webClient;
	}

	@Override
	public AsyncHttpClientRequest createAsyncOf(EndpointRequest endpointRequest) {
		return new VertxHttpClientRequest(webClient, endpointRequest, DEFAULT_VERTX_CHARSET);
	}

	@Override
	public void close() throws IOException {
		webClient.close();
	}
}
