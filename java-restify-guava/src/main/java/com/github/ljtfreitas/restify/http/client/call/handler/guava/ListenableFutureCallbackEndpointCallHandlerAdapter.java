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
package com.github.ljtfreitas.restify.http.client.call.handler.guava;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.util.async.DisposableExecutors;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class ListenableFutureCallbackEndpointCallHandlerAdapter<T, O> implements AsyncEndpointCallHandlerAdapter<Void, T, O> {

	private final ListeningExecutorService executorService;

	public ListenableFutureCallbackEndpointCallHandlerAdapter() {
		this(DisposableExecutors.newCachedThreadPool());
	}

	public ListenableFutureCallbackEndpointCallHandlerAdapter(ExecutorService executorService) {
		this(MoreExecutors.listeningDecorator(executorService));
	}

	public ListenableFutureCallbackEndpointCallHandlerAdapter(ListeningExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		EndpointMethodParameters parameters = endpointMethod.parameters();
		return endpointMethod.runnableAsync()
				&& (!parameters.callbacks(FutureCallback.class).isEmpty());
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return JavaType.of(callbackArgumentType(endpointMethod.parameters().callbacks()));
	}

	private Type callbackArgumentType(Collection<EndpointMethodParameter> callbackMethodParameters) {
		return callbackMethodParameters.stream()
				.filter(p -> p.javaType().is(FutureCallback.class))
					.findFirst()
						.map(p -> p.javaType().parameterized() ? p.javaType().as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class)
							.orElse(Object.class);
	}

	@Override
	public AsyncEndpointCallHandler<Void, O> adaptAsync(EndpointMethod endpointMethod, EndpointCallHandler<T, O> handler) {
		return new ListenableFutureCallbackEndpointMethodHandler(endpointMethod.parameters().callbacks(), handler);
	}

	private class ListenableFutureCallbackEndpointMethodHandler implements AsyncEndpointCallHandler<Void, O> {

		private final Collection<EndpointMethodParameter> callbackMethodParameters;
		private final EndpointCallHandler<T, O> delegate;

		public ListenableFutureCallbackEndpointMethodHandler(Collection<EndpointMethodParameter> callbackMethodParameters,
				EndpointCallHandler<T, O> handler) {
			this.callbackMethodParameters = callbackMethodParameters;
			this.delegate = handler;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Void handleAsync(AsyncEndpointCall<O> call, Object[] args) {
			FutureCallback<T> callback = callbackParameter(args);

			Future<T> future = call.executeAsync()
					.thenApplyAsync(o -> delegate.handle(() -> o, args), executorService)
						.toCompletableFuture();

			ListenableFuture<T> listenableFuture = JdkFutureAdapters.listenInPoolThread(future, executorService);
			Futures.addCallback(listenableFuture, callback, executorService);

			return null;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private FutureCallback<T> callbackParameter(Object[] args) {
			return callbackMethodParameters.stream()
					.filter(p -> p.javaType().is(FutureCallback.class))
						.findFirst()
							.map(p -> (FutureCallback) args[p.position()])
								.orElse(null);
		}
	}
}
