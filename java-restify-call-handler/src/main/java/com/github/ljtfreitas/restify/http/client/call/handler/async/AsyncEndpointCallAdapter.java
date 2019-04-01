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
package com.github.ljtfreitas.restify.http.client.call.handler.async;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.EndpointCallCallback;
import com.github.ljtfreitas.restify.http.client.call.async.EndpointCallFailureCallback;
import com.github.ljtfreitas.restify.http.client.call.async.EndpointCallSuccessCallback;
import com.github.ljtfreitas.restify.http.client.call.async.ExecutorAsyncEndpointCall;
import com.github.ljtfreitas.restify.util.async.DisposableExecutors;

class AsyncEndpointCallAdapter<T> implements AsyncEndpointCall<T> {

	private static final Executor DEFAULT_EXECUTOR = DisposableExecutors.newCachedThreadPool();

	private final ExecutorAsyncEndpointCall<T> delegate;

	public AsyncEndpointCallAdapter(EndpointCall<T> delegate) {
		this.delegate = new ExecutorAsyncEndpointCall<>(delegate, DEFAULT_EXECUTOR);
	}

	@Override
	public T execute() {
		return delegate.execute();
	}

	@Override
	public void executeAsync(EndpointCallCallback<T> callback) {
		delegate.executeAsync(callback);
	}

	@Override
	public void executeAsync(EndpointCallSuccessCallback<T> success, EndpointCallFailureCallback failure) {
		delegate.executeAsync(success, failure);
	}

	@Override
	public CompletionStage<T> executeAsync() {
		return delegate.executeAsync();
	}
}
