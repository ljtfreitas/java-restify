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

import java.util.Optional;

import org.reactivestreams.Publisher;

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.FallbackProvider;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.FallbackStrategy;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix.HystrixCommandMetadataFactory;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.util.Try;
import com.netflix.hystrix.HystrixObservableCommand;

import rx.Observable;
import rx.RxReactiveStreams;

class HystrixPublisherEndpointCallHandler<T, O> implements AsyncEndpointCallHandler<Publisher<T>, O> {

	private final HystrixObservableCommand.Setter properties;
	private final EndpointMethod endpointMethod;
	private final EndpointCallHandler<? extends Publisher<T>, O> delegate;
	private final FallbackProvider fallback;
	private final HystrixCommandMetadataFactory hystrixCommandMetadataFactory;

	HystrixPublisherEndpointCallHandler(HystrixObservableCommand.Setter properties, EndpointMethod endpointMethod,
			EndpointCallHandler<? extends Publisher<T>, O> delegate, FallbackProvider fallback, HystrixCommandMetadataFactory hystrixCommandMetadataFactory) {
		this.properties = properties;
		this.endpointMethod = endpointMethod;
		this.delegate = delegate;
		this.fallback = fallback;
		this.hystrixCommandMetadataFactory = hystrixCommandMetadataFactory;
	}

	@Override
	public JavaType returnType() {
		return delegate.returnType();
	}

	@Override
	public Publisher<T> handleAsync(AsyncEndpointCall<O> call, Object[] args) {
		PublisherObservableCommand command = new PublisherObservableCommand(hystrixProperties(), call, args);

		return RxReactiveStreams.toPublisher(command.toObservable());
	}

	private HystrixObservableCommand.Setter hystrixProperties() {
		return Optional.ofNullable(properties)
				.orElseGet(() -> hystrixCommandMetadataFactory.create(endpointMethod).asObservableCommand());
	}

	private class PublisherObservableCommand extends HystrixObservableCommand<T> {

		private final AsyncEndpointCall<O> call;
		private final Object[] args;

		private PublisherObservableCommand(HystrixObservableCommand.Setter setter, AsyncEndpointCall<O> call, Object[] args) {
			super(setter);
			this.call = call;
			this.args = args;
		}

		@Override
		protected Observable<T> construct() {
			return RxReactiveStreams.toObservable(delegate.handle(call, args));
		}

		@Override
		protected Observable<T> resumeWithFallback() {
			return fallback == null ?
						super.resumeWithFallback() :
							Try.of(this::doFallback).recover(e -> Try.success(Observable.error(e))).get();
		}

		private Observable<T> doFallback() {
			FallbackStrategy strategy = fallback.provides(endpointMethod.javaMethod())
					.strategy(endpointMethod.javaMethod().getDeclaringClass());

			PublisherFallbackResult<T> result = new PublisherFallbackResult<>(strategy.execute(endpointMethod.javaMethod(), args, getExecutionException()));

			return RxReactiveStreams.toObservable(result.get());
		}
	}
}
