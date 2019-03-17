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
package com.github.ljtfreitas.restify.http.client.call.handler.rxjava;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

import rx.Emitter;
import rx.Emitter.BackpressureMode;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class RxJavaObservableEndpointCallHandlerAdapter<T, O> implements AsyncEndpointCallHandlerAdapter<Observable<T>, Collection<T>, O> {

	public final Scheduler scheduler;

	public RxJavaObservableEndpointCallHandlerAdapter() {
		this(Schedulers.io());
	}

	public RxJavaObservableEndpointCallHandlerAdapter(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Observable.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return JavaType.parameterizedType(Collection.class, unwrap(endpointMethod.returnType()));
	}

	private Type unwrap(JavaType declaredReturnType) {
		return declaredReturnType.parameterized() ?
				declaredReturnType.as(ParameterizedType.class).getActualTypeArguments()[0] :
					Object.class;
	}

	@Override
	public AsyncEndpointCallHandler<Observable<T>, O> adaptAsync(EndpointMethod endpointMethod, EndpointCallHandler<Collection<T>, O> handler) {
		return new RxJavaObservableEndpointCallHandler(handler);
	}

	private class RxJavaObservableEndpointCallHandler implements AsyncEndpointCallHandler<Observable<T>, O> {

		private EndpointCallHandler<Collection<T>, O> delegate;

		public RxJavaObservableEndpointCallHandler(EndpointCallHandler<Collection<T>, O> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Observable<T> handleAsync(AsyncEndpointCall<O> call, Object[] args) {
			return Observable.create(new CompletionStageObservableEmitter(call.executeAsync()), BackpressureMode.NONE)
				.onErrorResumeNext(this::handleAsyncException)
					.map(o -> delegate.handle(() -> o, args))
						.flatMap(Observable::from)
							.subscribeOn(scheduler);
		}

		private Observable<O> handleAsyncException(Throwable throwable) {
			return Observable.error(
				(ExecutionException.class.equals(throwable.getClass()) || CompletionException.class.equals(throwable.getClass())) ?
						throwable.getCause() :
							throwable);
		}
	}

	private class CompletionStageObservableEmitter implements Action1<Emitter<O>> {

		private final CompletionStage<O> stage;

		private CompletionStageObservableEmitter(CompletionStage<O> stage) {
			this.stage = stage;
		}

		@Override
		public void call(Emitter<O> emitter) {
			CompletableFuture<O> future = stage.toCompletableFuture();

			future.whenComplete((value, throwable) -> {
				if (throwable != null) {
					emitter.onError(throwable);
				} else {
					emitter.onNext(value);
					emitter.onCompleted();
				}
			});

			emitter.setCancellation(() -> future.cancel(true));
		}
	}
}
