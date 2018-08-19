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

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class FluxEndpointCallHandlerAdapter<T, O> implements AsyncEndpointCallHandlerAdapter<Flux<T>, T, O> {

	public final Scheduler scheduler;

	public FluxEndpointCallHandlerAdapter() {
		this.scheduler = Schedulers.elastic();
	}

	public FluxEndpointCallHandlerAdapter(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Flux.class);
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
	public AsyncEndpointCallHandler<Flux<T>, O> adaptAsync(EndpointMethod endpointMethod, EndpointCallHandler<T, O> handler) {
		return new FluxEndpointCallHandler(handler);
	}

	private class FluxEndpointCallHandler implements AsyncEndpointCallHandler<Flux<T>, O> {

		private final MonoEndpointCallHandler<T, O> delegate;

		public FluxEndpointCallHandler(EndpointCallHandler<T, O> delegate) {
			this.delegate = new MonoEndpointCallHandler<>(delegate, scheduler);
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Flux<T> handleAsync(AsyncEndpointCall<O> call, Object[] args) {
			Mono<T> mono = delegate.handleAsync(call, args);

			return mono
				.flux()
					.subscribeOn(scheduler);
		}
	}
}
