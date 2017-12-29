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
package com.github.ljtfreitas.restify.http.netflix.client.call.exec;

import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.http.netflix.client.request.circuitbreaker.OnCircuitBreaker;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommand.Setter;

public abstract class BaseHystrixCircuitBreakerEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<T, T, O> {

	private final HystrixCommand.Setter hystrixMetadata;
	private final Object fallback;

	protected BaseHystrixCircuitBreakerEndpointCallExecutableFactory() {
		this(null, null);
	}

	protected BaseHystrixCircuitBreakerEndpointCallExecutableFactory(Setter hystrixMetadata) {
		this(hystrixMetadata, null);
	}

	protected BaseHystrixCircuitBreakerEndpointCallExecutableFactory(Setter hystrixMetadata, Object fallback) {
		this.hystrixMetadata = hystrixMetadata;
		this.fallback = fallback;
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return endpointMethod.returnType();
	}

	@Override
	public final boolean supports(EndpointMethod endpointMethod) {
		return onCircuitBreaker(endpointMethod)
				&& !returnHystrixCommand(endpointMethod)
					&& (fallback == null || sameTypeOfFallback(endpointMethod.javaMethod().getDeclaringClass()));
	}

	private boolean sameTypeOfFallback(Class<?> classType) {
		return classType.isAssignableFrom(fallback.getClass());
	}

	private boolean onCircuitBreaker(EndpointMethod endpointMethod) {
		return endpointMethod.metadata().contains(OnCircuitBreaker.class);
	}

	private boolean returnHystrixCommand(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(HystrixCommand.class);
	}

	@Override
	public EndpointCallExecutable<T, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> delegate) {
		return new HystrixCircuitBreakerEndpointCallExecutable<T, O>(hystrixMetadata(endpointMethod), endpointMethod, delegate, fallback(endpointMethod));
	}

	private HystrixCommand.Setter hystrixMetadata(EndpointMethod endpointMethod) {
		return Optional.ofNullable(hystrixMetadata).orElseGet(() -> buildHystrixMetadata(endpointMethod));
	}

	private HystrixCommand.Setter buildHystrixMetadata(EndpointMethod endpointMethod) {
		return new HystrixCommandMetadataFactory(endpointMethod).create();
	}

	private Object fallback(EndpointMethod endpointMethod) {
		return fallback == null ? fallbackTo(endpointMethod) : fallback;
	}

	protected Object fallbackTo(EndpointMethod endpointMethod) {
		return null;
	}
}
