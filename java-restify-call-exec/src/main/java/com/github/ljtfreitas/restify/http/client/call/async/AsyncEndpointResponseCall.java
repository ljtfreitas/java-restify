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

import com.github.ljtfreitas.restify.http.client.call.EndpointResponseCall;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

class AsyncEndpointResponseCall<T> implements AsyncEndpointCall<EndpointResponse<T>> {

	private final EndpointRequest endpointRequest;
	private final AsyncEndpointRequestExecutor asyncEndpointRequestExecutor;
	private final Executor executor;
	private final EndpointResponseCall<T> delegate;

	public AsyncEndpointResponseCall(EndpointRequest endpointRequest, AsyncEndpointRequestExecutor asyncEndpointRequestExecutor,
			Executor executor) {
		this.endpointRequest = endpointRequest;
		this.asyncEndpointRequestExecutor = asyncEndpointRequestExecutor;
		this.executor = executor;
		this.delegate = new EndpointResponseCall<>(endpointRequest, asyncEndpointRequestExecutor);
	}

	@Override
	public EndpointResponse<T> execute() {
		return delegate.execute();
	}

	@Override
	public void executeAsync(EndpointCallCallback<EndpointResponse<T>> callback) {
		new CompletableFutureAsyncEndpointCall<>(doExecuteAsync(), executor)
			.executeAsync(callback);
	}

	@Override
	public void executeAsync(EndpointCallSuccessCallback<EndpointResponse<T>> success,
			EndpointCallFailureCallback failure) {
		new CompletableFutureAsyncEndpointCall<>(doExecuteAsync(), executor)
			.executeAsync(success, failure);
	}

	@Override
	public CompletableFuture<EndpointResponse<T>> executeAsync() {
		return doExecuteAsync();
	}

	private CompletableFuture<EndpointResponse<T>> doExecuteAsync() {
		return asyncEndpointRequestExecutor.executeAsync(endpointRequest);
	}
}
