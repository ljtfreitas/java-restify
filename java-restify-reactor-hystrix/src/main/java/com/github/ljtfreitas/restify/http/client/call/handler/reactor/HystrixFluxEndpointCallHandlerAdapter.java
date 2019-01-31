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

import java.util.Collection;

import org.reactivestreams.Publisher;

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.Fallback;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.FallbackProvider;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreaker;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadataResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixObservableCommand;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class HystrixFluxEndpointCallHandlerAdapter<T, O> implements AsyncEndpointCallHandlerAdapter<Flux<T>, Collection<T>, O> {

	private final Scheduler scheduler;
	private final FluxEndpointCallHandlerAdapter<T, O> adapterToFlux;
	private final HystrixPublisherEndpointCallHandlerAdapter<T, O, Flux<T>> adapterToPublisher;

	public HystrixFluxEndpointCallHandlerAdapter() {
		this(Schedulers.elastic(), null, null, null);
	}

	public HystrixFluxEndpointCallHandlerAdapter(HystrixObservableCommand.Setter properties) {
		this(Schedulers.elastic(), null, null, null);
	}

	public HystrixFluxEndpointCallHandlerAdapter(Fallback fallback) {
		this((FallbackProvider) m -> fallback);
	}

	public HystrixFluxEndpointCallHandlerAdapter(FallbackProvider fallback) {
		this(Schedulers.elastic(), null, fallback, null);
	}

	public HystrixFluxEndpointCallHandlerAdapter(OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		this(Schedulers.elastic(), null, null, null);
	}

	public HystrixFluxEndpointCallHandlerAdapter(Scheduler scheduler, HystrixObservableCommand.Setter properties) {
		this(scheduler, properties, null, null);
	}

	public HystrixFluxEndpointCallHandlerAdapter(Scheduler scheduler, Fallback fallback) {
		this(scheduler, (FallbackProvider) m -> fallback);
	}

	public HystrixFluxEndpointCallHandlerAdapter(Scheduler scheduler, FallbackProvider fallback) {
		this(scheduler, null, fallback, null);
	}

	public HystrixFluxEndpointCallHandlerAdapter(Scheduler scheduler,
			OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		this(scheduler, null, null, onCircuitBreakerMetadataResolver);
	}

	public HystrixFluxEndpointCallHandlerAdapter(HystrixObservableCommand.Setter properties, Fallback fallback) {
		this(properties, (FallbackProvider) m -> fallback);
	}

	public HystrixFluxEndpointCallHandlerAdapter(HystrixObservableCommand.Setter properties,
			FallbackProvider fallback) {
		this(Schedulers.elastic(), properties, fallback);
	}

	public HystrixFluxEndpointCallHandlerAdapter(Fallback fallback,
			OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		this((FallbackProvider) m -> fallback, onCircuitBreakerMetadataResolver);
	}

	public HystrixFluxEndpointCallHandlerAdapter(FallbackProvider fallback,
			OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		this(Schedulers.elastic(), fallback, onCircuitBreakerMetadataResolver);
	}

	public HystrixFluxEndpointCallHandlerAdapter(Scheduler scheduler, HystrixObservableCommand.Setter properties,
			Fallback fallback) {
		this(scheduler, properties, (FallbackProvider) m -> fallback);
	}

	public HystrixFluxEndpointCallHandlerAdapter(Scheduler scheduler, Fallback fallback,
			OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		this(scheduler, (FallbackProvider) m -> fallback, onCircuitBreakerMetadataResolver);
	}

	public HystrixFluxEndpointCallHandlerAdapter(Scheduler scheduler, HystrixObservableCommand.Setter properties,
			FallbackProvider fallback) {
		this(Schedulers.elastic(), properties, fallback, null);
	}

	public HystrixFluxEndpointCallHandlerAdapter(Scheduler scheduler, FallbackProvider fallback,
			OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		this(Schedulers.elastic(), null, fallback, onCircuitBreakerMetadataResolver);
	}

	private HystrixFluxEndpointCallHandlerAdapter(Scheduler scheduler, HystrixObservableCommand.Setter properties,
			FallbackProvider fallback, OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		this.scheduler = scheduler;
		this.adapterToFlux = new FluxEndpointCallHandlerAdapter<>(scheduler);
		this.adapterToPublisher = new HystrixPublisherEndpointCallHandlerAdapter<>(properties, fallback,
				onCircuitBreakerMetadataResolver);
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Flux.class)
			&& endpointMethod.metadata().contains(OnCircuitBreaker.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return adapterToFlux.returnType(endpointMethod);
	}

	@Override
	public AsyncEndpointCallHandler<Flux<T>, O> adaptAsync(EndpointMethod endpointMethod, EndpointCallHandler<Collection<T>, O> delegate) {
		AsyncEndpointCallHandler<Flux<T>, O> handlerToFlux = adapterToFlux.adaptAsync(endpointMethod, delegate);

		return new HystrixFluxEndpointCalHandler(adapterToPublisher.adaptAsync(endpointMethod, handlerToFlux));
	}
	
	private class HystrixFluxEndpointCalHandler implements AsyncEndpointCallHandler<Flux<T>, O> {

		private final AsyncEndpointCallHandler<Publisher<T>, O> delegate;

		private HystrixFluxEndpointCalHandler(AsyncEndpointCallHandler<Publisher<T>, O> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Flux<T> handleAsync(AsyncEndpointCall<O> call, Object[] args) {
			Publisher<T> publisher = delegate.handleAsync(call, args);

			return Flux.from(publisher)
						.subscribeOn(scheduler);
		}
	}
}
