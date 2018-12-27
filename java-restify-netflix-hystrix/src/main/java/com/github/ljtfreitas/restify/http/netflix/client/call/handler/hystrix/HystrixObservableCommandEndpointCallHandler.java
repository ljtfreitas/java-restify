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
package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.FallbackProvider;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.FallbackStrategy;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.util.Tryable;
import com.netflix.hystrix.HystrixObservableCommand;

import rx.Observable;

class HystrixObservableCommandEndpointCallHandler<T, O> implements AsyncEndpointCallHandler<HystrixObservableCommand<T>, O> {

	private final HystrixObservableCommand.Setter properties;
	private final EndpointMethod endpointMethod;
	private final EndpointCallHandler<T, O> delegate;
	private final FallbackProvider fallback;
	private final HystrixCommandMetadataCache hystrixCommandMetadataCache = HystrixCommandMetadataCache.instance();

	HystrixObservableCommandEndpointCallHandler(HystrixObservableCommand.Setter properties, EndpointMethod endpointMethod,
			EndpointCallHandler<T, O> delegate, FallbackProvider fallback) {
		this.properties = properties;
		this.endpointMethod = endpointMethod;
		this.delegate = delegate;
		this.fallback = fallback;
	}

	@Override
	public JavaType returnType() {
		return delegate.returnType();
	}

	@Override
	public HystrixObservableCommand<T> handleAsync(AsyncEndpointCall<O> call, Object[] args) {
		return new HystrixObservableCommand<T>(hystrixProperties()) {

			@Override
			protected Observable<T> construct() {
				return Observable.from(call.executeAsync().toCompletableFuture())
					.map(o -> delegate.handle(() -> o, args));
			}

			@Override
			protected Observable<T> resumeWithFallback() {
				return fallback == null ? super.resumeWithFallback() : Tryable.of(this::doFallback);
			}

			private Observable<T> doFallback() throws Exception {
				FallbackStrategy strategy = fallback.provides(endpointMethod.javaMethod())
						.strategy(endpointMethod.javaMethod().getDeclaringClass());

				HystrixObservableFallbackResult<T> result = new HystrixObservableFallbackResult<>(strategy.execute(endpointMethod.javaMethod(), args));

				return result.get();
			}
		};
	}

	private HystrixObservableCommand.Setter hystrixProperties() {
		return Optional.ofNullable(properties)
				.orElseGet(() ->
					hystrixCommandMetadataCache.get(endpointMethod)
						.orElseGet(this::buildHystrixProperties).asObservableCommand());
	}

	private HystrixCommandMetadata buildHystrixProperties() {
		return hystrixCommandMetadataCache.put(endpointMethod, new HystrixCommandMetadataFactory(endpointMethod).create());
	}
}
