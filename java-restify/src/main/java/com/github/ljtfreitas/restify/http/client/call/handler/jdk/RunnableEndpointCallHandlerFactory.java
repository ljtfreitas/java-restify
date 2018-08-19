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

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class RunnableEndpointCallHandlerFactory implements EndpointCallHandlerFactory<Runnable, Void> {

	private static final RunnableEndpointCallHandlerFactory DEFAULT_INSTANCE = new RunnableEndpointCallHandlerFactory();

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Runnable.class);
	}

	@Override
	public EndpointCallHandler<Runnable, Void> create(EndpointMethod endpointMethod) {
		return new RunnableEndpointMethodHandler(JavaType.of(Void.class));
	}

	private class RunnableEndpointMethodHandler implements EndpointCallHandler<Runnable, Void> {

		private final JavaType type;

		private RunnableEndpointMethodHandler(JavaType type) {
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public Runnable handle(EndpointCall<Void> call, Object[] args) {
			return () -> call.execute();
		}
	}

	public static RunnableEndpointCallHandlerFactory instance() {
		return DEFAULT_INSTANCE;
	}
}
