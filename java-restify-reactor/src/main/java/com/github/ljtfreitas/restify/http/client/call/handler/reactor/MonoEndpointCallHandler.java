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
package com.github.ljtfreitas.restify.http.client.call.handler.reactor;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

class MonoEndpointCallHandler<T, O> implements AsyncEndpointCallHandler<Mono<T>, O> {

	private final EndpointCallHandler<T, O> delegate;
	private final Scheduler scheduler;

	public MonoEndpointCallHandler(EndpointCallHandler<T, O> delegate, Scheduler scheduler) {
		this.delegate = delegate;
		this.scheduler = scheduler;
	}

	@Override
	public JavaType returnType() {
		return delegate.returnType();
	}

	@Override
	public Mono<T> handle(EndpointCall<O> call, Object[] args) {
		return Mono.fromCallable(() -> delegate.handle(call, args))
			.subscribeOn(scheduler);
	}

	@Override
	public Mono<T> handleAsync(AsyncEndpointCall<O> call, Object[] args) {
		return Mono.fromFuture(call.executeAsync().toCompletableFuture())
			.onErrorMap(this::handleAsyncException)
				.subscribeOn(scheduler)
					.map(o -> delegate.handle(() -> o, args));
	}

	private Throwable handleAsyncException(Throwable throwable) {
		return (ExecutionException.class.equals(throwable.getClass()) || CompletionException.class.equals(throwable.getClass())) ?
				throwable.getCause() :
					throwable;
	}
}