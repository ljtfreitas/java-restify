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
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class OptionalEndpointCallHandlerFactory<T> implements EndpointCallHandlerFactory<Optional<T>, T> {

	private static final OptionalEndpointCallHandlerFactory<Object> DEFAULT_INSTANCE = new OptionalEndpointCallHandlerFactory<>();

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Optional.class);
	}

	@Override
	public EndpointCallHandler<Optional<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new OptionalEndpointCallHandler(JavaType.of(responseType));
	}

	private class OptionalEndpointCallHandler implements EndpointCallHandler<Optional<T>, T> {

		private final JavaType returnType;

		public OptionalEndpointCallHandler(JavaType returnType) {
			this.returnType = returnType;
		}

		@Override
		public JavaType returnType() {
			return returnType;
		}

		@Override
		public Optional<T> handle(EndpointCall<T> call, Object[] args) {
			return Optional.ofNullable(call.execute());
		}
	}

	public static OptionalEndpointCallHandlerFactory<Object> instance() {
		return DEFAULT_INSTANCE;
	}
}
