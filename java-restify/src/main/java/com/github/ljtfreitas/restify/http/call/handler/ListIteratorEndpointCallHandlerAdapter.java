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
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class ListIteratorEndpointCallHandlerAdapter<T> implements EndpointCallHandlerAdapter<ListIterator<T>, List<T>, List<T>> {

	private static final ListIteratorEndpointCallHandlerAdapter<Object> DEFAULT_INSTANCE = new ListIteratorEndpointCallHandlerAdapter<>();

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(ListIterator.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return listTypeOf(endpointMethod.returnType());
	}

	private JavaType listTypeOf(JavaType type) {
		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;
		return JavaType.parameterizedType(List.class, null, responseType);
	}

	@Override
	public EndpointCallHandler<ListIterator<T>, List<T>> adapt(EndpointMethod endpointMethod, EndpointCallHandler<List<T>, List<T>> delegate) {
		return new ListIteratorEndpointCallHandler(delegate);
	}

	private class ListIteratorEndpointCallHandler implements EndpointCallHandler<ListIterator<T>, List<T>> {

		private final EndpointCallHandler<List<T>, List<T>> delegate;

		public ListIteratorEndpointCallHandler(EndpointCallHandler<List<T>, List<T>> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public ListIterator<T> handle(EndpointCall<List<T>> call, Object[] args) {
			return Optional.ofNullable(delegate.handle(call, args))
				.map(c -> c.listIterator())
					.orElseGet(Collections::emptyListIterator);
		}
	}

	public static ListIteratorEndpointCallHandlerAdapter<Object> instance() {
		return DEFAULT_INSTANCE;
	}
}
