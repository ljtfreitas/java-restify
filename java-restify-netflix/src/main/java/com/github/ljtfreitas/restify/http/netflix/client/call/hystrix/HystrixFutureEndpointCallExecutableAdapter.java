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

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.Future;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixCommand;

public class HystrixFutureEndpointCallExecutableAdapter<T, O> implements EndpointCallExecutableAdapter<Future<T>, HystrixCommand<T>, O> {

	private final HystrixEndpointCallExecutableAdapter<T, O> hystrixEndpointCallExecutableAdapter = new HystrixEndpointCallExecutableAdapter<>();

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return hystrixEndpointCallExecutableAdapter.supports(endpointMethod) && endpointMethod.returnType().is(Future.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return hystrixEndpointCallExecutableAdapter.returnType(endpointMethod.returns(unwrap(endpointMethod.returnType())));
	}
	
	private JavaType unwrap(JavaType declaredReturnType) {
		return JavaType.of(declaredReturnType.parameterized() ?
				declaredReturnType.as(ParameterizedType.class).getActualTypeArguments()[0] :
					Object.class);
	}

	@Override
	public EndpointCallExecutable<Future<T>, O> adapt(EndpointMethod endpointMethod, EndpointCallExecutable<HystrixCommand<T>, O> executable) {
		return new HystrixFutureEndpointCallExecutable(executable);
	}

	private class HystrixFutureEndpointCallExecutable implements EndpointCallExecutable<Future<T>, O> {

		private final EndpointCallExecutable<HystrixCommand<T>, O> delegate;

		private HystrixFutureEndpointCallExecutable(EndpointCallExecutable<HystrixCommand<T>, O> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}
		
		@Override
		public Future<T> execute(EndpointCall<O> call, Object[] args) {
			HystrixCommand<T> command = delegate.execute(call, args);
			return command.queue();
		}
	}
}
