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
package com.github.ljtfreitas.restify.http.client.call.exec.guava;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutableDecoratorFactory;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class ListenableFutureEndpointCallExecutableFactory<T, O> implements AsyncEndpointCallExecutableDecoratorFactory<ListenableFuture<T>, T, O> {

	private final ListeningExecutorService executorService;

	public ListenableFutureEndpointCallExecutableFactory() {
		this(MoreExecutors.listeningDecorator(Executors.newCachedThreadPool()));
	}

	public ListenableFutureEndpointCallExecutableFactory(ListeningExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(ListenableFuture.class);
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
	public AsyncEndpointCallExecutable<ListenableFuture<T>, O> createAsync(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new ListenableFutureEndpointMethodExecutable(executable);
	}

	private class ListenableFutureEndpointMethodExecutable implements AsyncEndpointCallExecutable<ListenableFuture<T>, O> {

		private final EndpointCallExecutable<T, O> delegate;

		public ListenableFutureEndpointMethodExecutable(EndpointCallExecutable<T, O> executable) {
			this.delegate = executable;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public ListenableFuture<T> execute(EndpointCall<O> call, Object[] args) {
			return executorService.submit(() -> delegate.execute(call, args));
		}

		@Override
		public ListenableFuture<T> executeAsync(AsyncEndpointCall<O> call, Object[] args) {
			Future<T> future = call.executeAsync()
					.thenApplyAsync(o -> delegate.execute(() -> o, args), executorService);

			return JdkFutureAdapters.listenInPoolThread(future, executorService);
		}
	}
}
