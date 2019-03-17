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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import org.reactivestreams.Publisher;

import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.FallbackProvider;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreaker;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadataResolver;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.WithFallbackProvider;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix.HystrixCommandMetadataFactory;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixObservableCommand;

class HystrixPublisherEndpointCallHandlerAdapter<T, O, Y extends Publisher<T>> implements AsyncEndpointCallHandlerAdapter<Publisher<T>, Y, O> {

	private final HystrixObservableCommand.Setter properties;
	private final FallbackProvider fallback;
	private final HystrixCommandMetadataFactory hystrixCommandMetadataFactory;

	public HystrixPublisherEndpointCallHandlerAdapter(HystrixObservableCommand.Setter properties, FallbackProvider fallback,
			OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		this.properties = properties;
		this.fallback = Optional.ofNullable(fallback).orElseGet(WithFallbackProvider::new);
		this.hystrixCommandMetadataFactory = Optional.ofNullable(onCircuitBreakerMetadataResolver).map(HystrixCommandMetadataFactory::new)
				.orElseGet(HystrixCommandMetadataFactory::new);
	}

	@Override
	public final boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Publisher.class) && endpointMethod.metadata().contains(OnCircuitBreaker.class);
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
	public AsyncEndpointCallHandler<Publisher<T>, O> adaptAsync(EndpointMethod endpointMethod, EndpointCallHandler<Y, O> handler) {
		return new HystrixPublisherEndpointCallHandler<T, O>(properties, endpointMethod, handler, fallback, hystrixCommandMetadataFactory);
	}
}
