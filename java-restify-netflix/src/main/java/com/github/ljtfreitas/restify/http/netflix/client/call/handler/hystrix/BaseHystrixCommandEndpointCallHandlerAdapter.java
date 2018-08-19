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

import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixCommand;

public abstract class BaseHystrixCommandEndpointCallHandlerAdapter<T, O> implements EndpointCallHandlerAdapter<HystrixCommand<T>, T, O> {

	private final HystrixCommand.Setter hystrixMetadata;
	private final Object fallback;

	protected BaseHystrixCommandEndpointCallHandlerAdapter() {
		this(null, null);
	}

	protected BaseHystrixCommandEndpointCallHandlerAdapter(HystrixCommand.Setter hystrixMetadata) {
		this(hystrixMetadata, null);
	}

	protected BaseHystrixCommandEndpointCallHandlerAdapter(HystrixCommand.Setter hystrixMetadata, Object fallback) {
		this.hystrixMetadata = hystrixMetadata;
		this.fallback = fallback;
	}

	@Override
	public final boolean supports(EndpointMethod endpointMethod) {
		return returnHystrixCommand(endpointMethod)
				&& (fallback == null || sameTypeOfFallback(endpointMethod.javaMethod().getDeclaringClass()));
	}

	private boolean returnHystrixCommand(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(HystrixCommand.class);
	}

	private boolean sameTypeOfFallback(Class<?> classType) {
		return classType.isAssignableFrom(fallback.getClass());
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
	public EndpointCallHandler<HystrixCommand<T>, O> adapt(EndpointMethod endpointMethod, EndpointCallHandler<T, O> delegate) {
		return new HystrixCommandEndpointCallHandler<T, O>(hystrixMetadata, endpointMethod, delegate, fallback(endpointMethod));
	}

	private Object fallback(EndpointMethod endpointMethod) {
		return fallback == null ? fallbackTo(endpointMethod) : fallback;
	}

	protected Object fallbackTo(EndpointMethod endpointMethod) {
		return null;
	}
}
