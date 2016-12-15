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

import java.lang.reflect.Method;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaAnnotationScanner;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;
import com.github.ljtfreitas.restify.http.netflix.hystrix.OnCircuitBreaker;
import com.netflix.hystrix.HystrixCommand;

public class HystrixCircuitBreakerEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<T, T, O> {

	private final HystrixCommand.Setter hystrixMetadata;

	public HystrixCircuitBreakerEndpointCallExecutableFactory() {
		this.hystrixMetadata = null;
	}

	public HystrixCircuitBreakerEndpointCallExecutableFactory(HystrixCommand.Setter hystrixMetadata) {
		this.hystrixMetadata = hystrixMetadata;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		Method javaMethod = endpointMethod.javaMethod();
		return methodOnCircuitBreaker(javaMethod) || classOnCircuitBreaker(javaMethod.getDeclaringClass());
	}

	private boolean methodOnCircuitBreaker(Method javaMethod) {
		return new JavaAnnotationScanner(javaMethod).contains(OnCircuitBreaker.class);
	}

	private boolean classOnCircuitBreaker(Class<?> classType) {
		return new JavaAnnotationScanner(classType).contains(OnCircuitBreaker.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return endpointMethod.returnType();
	}

	@Override
	public EndpointCallExecutable<T, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> delegate) {
		return new HystrixCircuitBreakerEndpointCallExecutable<>(hystrixMetadata, endpointMethod, delegate);
	}
}
