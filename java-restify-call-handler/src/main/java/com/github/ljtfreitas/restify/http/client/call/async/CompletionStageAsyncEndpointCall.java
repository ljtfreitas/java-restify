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

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.github.ljtfreitas.restify.http.client.HttpException;

public class CompletionStageAsyncEndpointCall<T> implements AsyncEndpointCall<T> {

	private final CompletionStage<T> stage;
	private final Executor executor;

	public CompletionStageAsyncEndpointCall(CompletionStage<T> stage) {
		this(stage, ExecutorAsyncEndpointCall.pool());
	}

	public CompletionStageAsyncEndpointCall(CompletionStage<T> stage, Executor executor) {
		this.stage = stage;
		this.executor = executor;
	}

	@Override
	public T execute() {
		try {
			return stage.toCompletableFuture().get();
		} catch (InterruptedException e) {
			throw new HttpException("Interrupted exception on execute request.", e);

		} catch (ExecutionException e) {
			throw new HttpException(deepCause(e));
		}
	}

	@Override
	public void executeAsync(EndpointCallCallback<T> callback) {
		stage.whenCompleteAsync((r, e) -> handle(r, e, callback, callback), executor);
	}

	@Override
	public void executeAsync(EndpointCallSuccessCallback<T> success, EndpointCallFailureCallback failure) {
		stage.whenCompleteAsync((r, e) -> handle(r, e, success, failure), executor);
	}

	@Override
	public CompletionStage<T> executeAsync() {
		return stage;
	}

	private void handle(T value, Throwable throwable, EndpointCallSuccessCallback<T> successCallback, EndpointCallFailureCallback failureCallback) {
		if (throwable != null) {
			if (failureCallback != null) {
				Throwable cause = deepCause(throwable);

				failureCallback.onFailure(cause);
			}

		} else if (successCallback != null) {
			successCallback.onSuccess(value);

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
