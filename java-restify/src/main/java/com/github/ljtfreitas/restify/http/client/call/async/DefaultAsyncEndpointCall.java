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
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.request.async.EndpointCallCallback;
import com.github.ljtfreitas.restify.http.client.request.async.EndpointCallFailureCallback;
import com.github.ljtfreitas.restify.http.client.request.async.EndpointCallSuccessCallback;

class DefaultAsyncEndpointCall<T> implements AsyncEndpointCall<T> {

	private final Executor executor;
	private final EndpointCall<T> source;

	public DefaultAsyncEndpointCall(Executor executor, EndpointCall<T> source) {
		this.executor = executor;
		this.source = source;
	}

	@Override
	public void execute(EndpointCallCallback<T> callback) {
		CompletableFuture
			.supplyAsync(() -> source.execute(), executor)
				.whenComplete((r, e) -> handle(r, e, callback, callback));
	}

	@Override
	public void execute(EndpointCallSuccessCallback<T> successCallback, EndpointCallFailureCallback failureCallback) {
		CompletableFuture
			.supplyAsync(() -> source.execute(), executor)
				.whenComplete((r, e) -> handle(r, e, successCallback, failureCallback));
	}

	private void handle(T value, Throwable throwable, EndpointCallSuccessCallback<T> successCallback, EndpointCallFailureCallback failureCallback) {
		if (value != null && successCallback != null) {
			successCallback.onSuccess(value);

		} else if (throwable != null && failureCallback != null) {
			Throwable cause = deepCause(throwable);

			failureCallback.onFailure(cause);
		}
	}

	private Throwable deepCause(Throwable throwable) {
		if (throwable instanceof CompletionException) {
			return deepCause(throwable.getCause());
		} else {
			return throwable;
		}
	}
}
