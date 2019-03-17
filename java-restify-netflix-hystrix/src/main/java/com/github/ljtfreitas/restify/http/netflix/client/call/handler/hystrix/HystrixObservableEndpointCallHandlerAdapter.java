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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixObservableCommand;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class HystrixObservableEndpointCallHandlerAdapter<T, O> implements AsyncEndpointCallHandlerAdapter<Observable<T>, HystrixObservableCommand<T>, O> {

	private final Scheduler scheduler;
	private final HystrixOnCircuitBreakerPredicate predicate = new HystrixOnCircuitBreakerPredicate();

	public HystrixObservableEndpointCallHandlerAdapter() {
		this(Schedulers.io());
	}

	public HystrixObservableEndpointCallHandlerAdapter(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return predicate.test(endpointMethod)
			&& endpointMethod.returnType().is(Observable.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return JavaType.parameterizedType(HystrixObservableCommand.class, unwrap(endpointMethod.returnType()));
	}

	private Type unwrap(JavaType declaredReturnType) {
		return declaredReturnType.parameterized() ?
				declaredReturnType.as(ParameterizedType.class).getActualTypeArguments()[0] :
					Object.class;
	}

	@Override
	public AsyncEndpointCallHandler<Observable<T>, O> adaptAsync(EndpointMethod endpointMethod,
			EndpointCallHandler<HystrixObservableCommand<T>, O> handler) {
		return new HystrixObservableEndpointCallHandler(handler);
	}

	private class HystrixObservableEndpointCallHandler implements AsyncEndpointCallHandler<Observable<T>, O> {

		private final EndpointCallHandler<HystrixObservableCommand<T>, O> delegate;

		private HystrixObservableEndpointCallHandler(EndpointCallHandler<HystrixObservableCommand<T>, O> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Observable<T> handleAsync(AsyncEndpointCall<O> call, Object[] args) {
			HystrixObservableCommand<T> command = delegate.handle(call, args);
			return command.toObservable().subscribeOn(scheduler);
		}
	}
}
