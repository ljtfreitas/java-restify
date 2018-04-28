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
package com.github.ljtfreitas.restify.http.client.call.exec.async;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.CompletableFutureAsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class AsyncEndpointCallObjectExecutableFactory<T, O> implements AsyncEndpointCallExecutableDecoratorFactory<AsyncEndpointCall<T>, T, O> {

	private final Executor executor;

	public AsyncEndpointCallObjectExecutableFactory(Executor executor) {
		this.executor = executor;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(AsyncEndpointCall.class);
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
	public AsyncEndpointCallExecutable<AsyncEndpointCall<T>, O> createAsync(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new AsyncEndpointCallExecutableAdapter(executable);
	}

	private class AsyncEndpointCallExecutableAdapter implements AsyncEndpointCallExecutable<AsyncEndpointCall<T>, O> {

		private final EndpointCallExecutable<T, O> delegate;

		private AsyncEndpointCallExecutableAdapter(EndpointCallExecutable<T, O> executable) {
			this.delegate = executable;;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public AsyncEndpointCall<T> executeAsync(AsyncEndpointCall<O> call, Object[] args) {
			CompletableFuture<T> future = call.executeAsync()
					.thenApplyAsync(o -> delegate.execute(() -> o, args), executor);

			return new CompletableFutureAsyncEndpointCall<>(future, executor);
		}
	}
}
