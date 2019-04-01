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
package com.github.ljtfreitas.restify.http.spring.client.call.handler.async;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.scheduling.annotation.AsyncResult;

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.util.Try;

public class AsyncResultEndpointCallHandlerAdapter<T, O> implements AsyncEndpointCallHandlerAdapter<AsyncResult<T>, T, O> {

	private final Executor executor;

	public AsyncResultEndpointCallHandlerAdapter(Executor executor) {
		this.executor = executor;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(AsyncResult.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return JavaType.of(unwrap(endpointMethod.returnType()));
	}

	private Type unwrap(JavaType declaredReturnType) {
		return declaredReturnType.parameterized() ?
				declaredReturnType.as(ParameterizedType.class).getActualTypeArguments()[0] :
					Object.class;
	}

	@Override
	public AsyncEndpointCallHandler<AsyncResult<T>, O> adaptAsync(EndpointMethod endpointMethod, EndpointCallHandler<T, O> handler) {
		return new AsyncResultEndpointCallHandler(handler);
	}

	private class AsyncResultEndpointCallHandler implements AsyncEndpointCallHandler<AsyncResult<T>, O> {

		private final EndpointCallHandler<T, O> delegate;

		private AsyncResultEndpointCallHandler(EndpointCallHandler<T, O> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public AsyncResult<T> handleAsync(AsyncEndpointCall<O> call, Object[] args) {
			return Try.of(() -> doHandleAsync(call, args))
				.map(c -> c.join())
					.get();
		}

		private CompletableFuture<AsyncResult<T>> doHandleAsync(AsyncEndpointCall<O> call, Object[] args) {
			return call.executeAsync()
				.thenApplyAsync(o -> delegate.handle(() -> o, args), executor)
					.handleAsync(this::handle, executor)
						.toCompletableFuture();
		}

		@SuppressWarnings("unchecked")
		private AsyncResult<T> handle(T value, Throwable throwable) {
			return value != null ?
					(AsyncResult<T>) AsyncResult.forValue(value) :
						(AsyncResult<T>) AsyncResult.forExecutionException(throwable);
		}
	}
}
