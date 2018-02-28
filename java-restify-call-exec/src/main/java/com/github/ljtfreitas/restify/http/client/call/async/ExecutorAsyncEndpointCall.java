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
package com.github.ljtfreitas.restify.http.client.call.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;

public class ExecutorAsyncEndpointCall<T> implements AsyncEndpointCall<T> {

	private static final Executor DEFAULT_EXECUTOR = Executors.newCachedThreadPool();

	private final Executor executor;
	private final EndpointCall<T> source;

	public ExecutorAsyncEndpointCall(EndpointCall<T> source) {
		this(source, DEFAULT_EXECUTOR);
	}

	public ExecutorAsyncEndpointCall(EndpointCall<T> source, Executor executor) {
		this.source = source;
		this.executor = executor;
	}

	@Override
	public void executeAsync(EndpointCallCallback<T> callback) {
		new CompletableFutureAsyncEndpointCall<>(doExecuteAsync(), executor)
			.executeAsync(callback);
	}

	@Override
	public void executeAsync(EndpointCallSuccessCallback<T> success, EndpointCallFailureCallback failure) {
		new CompletableFutureAsyncEndpointCall<>(doExecuteAsync(), executor)
			.executeAsync(success, failure);
	}

	@Override
	public CompletableFuture<T> executeAsync() {
		return doExecuteAsync();
	}

	@Override
	public T execute() {
		return source.execute();
	}

	private CompletableFuture<T> doExecuteAsync() {
		return CompletableFuture
				.supplyAsync(() -> source.execute(), executor);
	}

	public static Executor pool() {
		return DEFAULT_EXECUTOR;
	}
}
