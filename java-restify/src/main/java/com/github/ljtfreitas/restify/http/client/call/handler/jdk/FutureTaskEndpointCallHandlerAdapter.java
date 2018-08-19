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
package com.github.ljtfreitas.restify.http.client.call.handler.jdk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class FutureTaskEndpointCallHandlerAdapter<T, O> implements EndpointCallHandlerAdapter<FutureTask<T>, T, O> {

	private final ExecutorService executorService;

	public FutureTaskEndpointCallHandlerAdapter(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(FutureTask.class);
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
	public EndpointCallHandler<FutureTask<T>, O> adapt(EndpointMethod endpointMethod, EndpointCallHandler<T, O> handler) {
		return new FutureTaskEndpointCallHandler(handler);
	}

	private class FutureTaskEndpointCallHandler implements EndpointCallHandler<FutureTask<T>, O> {

		private final EndpointCallHandler<T, O> delegate;

		public FutureTaskEndpointCallHandler(EndpointCallHandler<T, O> handler) {
			this.delegate = handler;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public FutureTask<T> handle(EndpointCall<O> call, Object[] args) {
			FutureTask<T> task = new FutureTask<T>(() -> delegate.handle(call, args));
			executorService.submit(task);
			return task;
		}
	}
}
