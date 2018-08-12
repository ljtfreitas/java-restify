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
package com.github.ljtfreitas.restify.http.netflix.client.call.hystrix;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixCommand;

public class HystrixEndpointCallExecutableAdapter<T, O> implements EndpointCallExecutableAdapter<T, HystrixCommand<T>, O> {

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return JavaType.of(JavaType.parameterizedType(HystrixCommand.class, endpointMethod.returnType().unwrap()));
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return onCircuitBreaker(endpointMethod)
			&& !returnHystrixCommand(endpointMethod);
	}

	private boolean onCircuitBreaker(EndpointMethod endpointMethod) {
		return endpointMethod.metadata().contains(OnCircuitBreaker.class);
	}

	private boolean returnHystrixCommand(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(HystrixCommand.class);
	}

	@Override
	public EndpointCallExecutable<T, O> adapt(EndpointMethod endpointMethod, EndpointCallExecutable<HystrixCommand<T>, O> delegate) {
		return new HystrixEndpointCallExecutable(delegate);
	}

	private class HystrixEndpointCallExecutable implements EndpointCallExecutable<T, O> {

		private final EndpointCallExecutable<HystrixCommand<T>, O> delegate;

		private HystrixEndpointCallExecutable(EndpointCallExecutable<HystrixCommand<T>, O> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public T execute(EndpointCall<O> call, Object[] args) {
			HystrixCommand<T> command = delegate.execute(call, args);
			return command.execute();
		}
	}
}
