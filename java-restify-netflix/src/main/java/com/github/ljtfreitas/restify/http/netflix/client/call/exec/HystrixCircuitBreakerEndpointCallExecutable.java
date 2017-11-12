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

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.util.Tryable;
import com.netflix.hystrix.HystrixCommand;

class HystrixCircuitBreakerEndpointCallExecutable<T, O> implements EndpointCallExecutable<T, O> {

	private final HystrixCommand.Setter hystrixMetadata;
	private final EndpointMethod endpointMethod;
	private final EndpointCallExecutable<T, O> delegate;
	private final Object fallback;

	public HystrixCircuitBreakerEndpointCallExecutable(HystrixCommand.Setter hystrixMetadata, EndpointMethod endpointMethod,
			EndpointCallExecutable<T, O> delegate) {

		this(hystrixMetadata, endpointMethod, delegate, null);
	}

	public HystrixCircuitBreakerEndpointCallExecutable(HystrixCommand.Setter hystrixMetadata, EndpointMethod endpointMethod,
			EndpointCallExecutable<T, O> delegate, Object fallback) {

		this.hystrixMetadata = hystrixMetadata;
		this.endpointMethod = endpointMethod;
		this.delegate = delegate;
		this.fallback = fallback;
	}

	@Override
	public JavaType returnType() {
		return delegate.returnType();
	}

	@Override
	public T execute(EndpointCall<O> call, Object[] args) {
		return delegate.execute(new HystrixEndpointCall(endpointMethod, call, args), args);
	}

	class HystrixEndpointCall implements EndpointCall<O> {

		private final EndpointMethod endpointMethod;
		private final EndpointCall<O> delegate;
		private Object[] args;

		public HystrixEndpointCall(EndpointMethod endpointMethod, EndpointCall<O> delegate, Object[] args) {
			this.endpointMethod = endpointMethod;
			this.delegate = delegate;
			this.args = args;
		}

		@Override
		public O execute() {
			HystrixCommand<O> hystrixCommand = new HystrixCommand<O>(hystrixMetadata()) {
				@Override
				protected O run() throws Exception {
					return delegate.execute();
				}

				@Override
				protected O getFallback() {
					return Optional.ofNullable(fallback)
							.map(f -> Tryable.of(() -> doFallback()))
								.orElseGet(() -> super.getFallback());
				}

				@SuppressWarnings("unchecked")
				private O doFallback() throws Exception {
					return (O) endpointMethod.javaMethod().invoke(fallback, args);
				}
			};

			return hystrixCommand.execute();
		}

		private HystrixCommand.Setter hystrixMetadata() {
			return Optional.ofNullable(hystrixMetadata)
					.orElseGet(() -> buildHystrixMetadata());
		}

		private HystrixCommand.Setter buildHystrixMetadata() {
			return new HystrixCommandMetadataFactory(endpointMethod).create();
		}
	}
}