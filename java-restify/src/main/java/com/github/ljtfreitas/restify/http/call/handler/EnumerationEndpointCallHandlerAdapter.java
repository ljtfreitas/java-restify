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
package com.github.ljtfreitas.restify.http.call.handler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class EnumerationEndpointCallHandlerAdapter<T> implements EndpointCallHandlerAdapter<Enumeration<T>, Collection<T>, Collection<T>> {

	private static final EnumerationEndpointCallHandlerAdapter<Object> DEFAULT_INSTANCE = new EnumerationEndpointCallHandlerAdapter<>();

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Enumeration.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return collectionTypeOf(endpointMethod.returnType());
	}

	private JavaType collectionTypeOf(JavaType type) {
		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;
		return JavaType.parameterizedType(Collection.class, null, responseType);
	}

	@Override
	public EndpointCallHandler<Enumeration<T>, Collection<T>> adapt(EndpointMethod endpointMethod, EndpointCallHandler<Collection<T>, Collection<T>> delegate) {
		return new EnumerationEndpointCallHandler(delegate);
	}

	private class EnumerationEndpointCallHandler implements EndpointCallHandler<Enumeration<T>, Collection<T>> {

		private final EndpointCallHandler<Collection<T>, Collection<T>> delegate;

		public EnumerationEndpointCallHandler(EndpointCallHandler<Collection<T>, Collection<T>> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Enumeration<T> handle(EndpointCall<Collection<T>> call, Object[] args) {
			return Collections.enumeration(delegate.handle(call, args));
		}
	}

	public static EnumerationEndpointCallHandlerAdapter<Object> instance() {
		return DEFAULT_INSTANCE;
	}
}
