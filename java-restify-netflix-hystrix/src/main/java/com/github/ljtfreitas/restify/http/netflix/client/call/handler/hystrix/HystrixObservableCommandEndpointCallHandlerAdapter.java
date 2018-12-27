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
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.Fallback;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.FallbackProvider;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.WithFallbackProvider;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixObservableCommand;

public class HystrixObservableCommandEndpointCallHandlerAdapter<T, O> implements AsyncEndpointCallHandlerAdapter<HystrixObservableCommand<T>, T, O> {

	protected final HystrixObservableCommand.Setter properties;
	protected final FallbackProvider fallback;

	public HystrixObservableCommandEndpointCallHandlerAdapter() {
		this(null, (FallbackProvider) null);
	}

	public HystrixObservableCommandEndpointCallHandlerAdapter(HystrixObservableCommand.Setter properties) {
		this(properties, (FallbackProvider) null);
	}

	public HystrixObservableCommandEndpointCallHandlerAdapter(Fallback fallback) {
		this(null, (FallbackProvider) (t) -> fallback);
	}

	public HystrixObservableCommandEndpointCallHandlerAdapter(FallbackProvider fallback) {
		this(null, (FallbackProvider) fallback);
	}

	public HystrixObservableCommandEndpointCallHandlerAdapter(HystrixObservableCommand.Setter properties, Fallback fallback) {
		this(properties, (FallbackProvider) (t) -> fallback);
	}

	public HystrixObservableCommandEndpointCallHandlerAdapter(HystrixObservableCommand.Setter properties, FallbackProvider fallback) {
		this.properties = properties;
		this.fallback = fallback;
	}

	@Override
	public final boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(HystrixObservableCommand.class);
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
	public AsyncEndpointCallHandler<HystrixObservableCommand<T>, O> adaptAsync(EndpointMethod endpointMethod,
			EndpointCallHandler<T, O> delegate) {
		return new HystrixObservableCommandEndpointCallHandler<>(properties, endpointMethod, delegate,
				Optional.ofNullable(fallback).orElseGet(WithFallbackProvider::new));
	}
}
