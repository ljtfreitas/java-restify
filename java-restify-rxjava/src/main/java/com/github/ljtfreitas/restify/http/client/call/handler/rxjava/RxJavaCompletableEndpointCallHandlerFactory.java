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

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

import rx.Completable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class RxJavaCompletableEndpointCallHandlerFactory implements AsyncEndpointCallHandlerFactory<Completable, Void> {

	private static final JavaType VOID_TYPE = JavaType.of(Void.class);

	public final Scheduler scheduler;

	public RxJavaCompletableEndpointCallHandlerFactory() {
		this.scheduler = Schedulers.io();
	}

	public RxJavaCompletableEndpointCallHandlerFactory(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Completable.class);
	}

	@Override
	public AsyncEndpointCallHandler<Completable, Void> createAsync(EndpointMethod endpointMethod) {
		return new RxJavaCompletableEndpointCallHandler();
	}

	private class RxJavaCompletableEndpointCallHandler implements AsyncEndpointCallHandler<Completable, Void> {

		@Override
		public JavaType returnType() {
			return VOID_TYPE;
		}

		@Override
		public Completable handle(EndpointCall<Void> call, Object[] args) {
			return Completable.fromAction(call::execute)
				.subscribeOn(scheduler);
		}

		@Override
		public Completable handleAsync(AsyncEndpointCall<Void> call, Object[] args) {
			return Completable.fromFuture(call.executeAsync().toCompletableFuture())
				.onErrorResumeNext(this::handleAsyncException)
					.subscribeOn(scheduler);
		}

		private Completable handleAsyncException(Throwable throwable) {
			return Completable.error(() ->
				(ExecutionException.class.equals(throwable.getClass()) || CompletionException.class.equals(throwable.getClass())) ?
						throwable.getCause() :
							throwable);
		}
	}
}
