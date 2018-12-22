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
package com.github.ljtfreitas.restify.http.client.call.handler.rxjava2;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class RxJava2ObservableEndpointCallHandlerAdapter<T, O> implements AsyncEndpointCallHandlerAdapter<Observable<T>, Collection<T>, O> {

	public final Scheduler scheduler;

	public RxJava2ObservableEndpointCallHandlerAdapter() {
		this.scheduler = Schedulers.io();
	}

	public RxJava2ObservableEndpointCallHandlerAdapter(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Observable.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return JavaType.of(JavaType.parameterizedType(Collection.class, unwrap(endpointMethod.returnType())));
	}

	private Type unwrap(JavaType declaredReturnType) {
		return declaredReturnType.parameterized() ?
				declaredReturnType.as(ParameterizedType.class).getActualTypeArguments()[0] :
					Object.class;
	}

	@Override
	public AsyncEndpointCallHandler<Observable<T>, O> adaptAsync(EndpointMethod endpointMethod, EndpointCallHandler<Collection<T>, O> handler) {
		return new RxJava2ObservableEndpointCallHandler(handler);
	}

	private class RxJava2ObservableEndpointCallHandler implements AsyncEndpointCallHandler<Observable<T>, O> {

		private EndpointCallHandler<Collection<T>, O> delegate;

		public RxJava2ObservableEndpointCallHandler(EndpointCallHandler<Collection<T>, O> handler) {
			this.delegate = handler;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Observable<T> handle(EndpointCall<O> call, Object[] args) {
			return Observable.defer(() -> Observable.fromIterable(delegate.handle(call, args)))
				.subscribeOn(scheduler);
		}

		@Override
		public Observable<T> handleAsync(AsyncEndpointCall<O> call, Object[] args) {
			return Observable.fromFuture(call.executeAsync().toCompletableFuture())
				.onErrorResumeNext(this::handleAsyncException)
				.map(o -> delegate.handle(() -> o, args))
					.flatMap(Observable::fromIterable)
						.subscribeOn(scheduler);
		}

		private Observable<O> handleAsyncException(Throwable throwable) {
			return Observable.error(() ->
				(ExecutionException.class.equals(throwable.getClass()) || CompletionException.class.equals(throwable.getClass())) ?
						throwable.getCause() :
							throwable);
		}
	}
}
