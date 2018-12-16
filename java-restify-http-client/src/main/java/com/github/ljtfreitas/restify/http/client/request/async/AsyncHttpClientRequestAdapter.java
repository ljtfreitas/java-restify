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
package com.github.ljtfreitas.restify.http.client.request.async;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;
import com.github.ljtfreitas.restify.util.async.DisposableExecutors;

public class AsyncHttpClientRequestAdapter implements AsyncHttpClientRequest  {

	private static final Executor DEFAULT_EXECUTOR = DisposableExecutors.newCachedThreadPool();

	private final HttpClientRequest source;
	private final Executor executor;

	public AsyncHttpClientRequestAdapter(HttpClientRequest source) {
		this(source, DEFAULT_EXECUTOR);
	}

	public AsyncHttpClientRequestAdapter(HttpClientRequest source, Executor executor) {
		this.source = source;
		this.executor = executor;
	}

	@Override
	public HttpClientResponse execute() throws HttpClientException {
		return source.execute();
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
	public CompletionStage<HttpClientResponse> executeAsync() throws HttpClientException {
		return CompletableFuture.supplyAsync(source::execute, executor);
	}
}
