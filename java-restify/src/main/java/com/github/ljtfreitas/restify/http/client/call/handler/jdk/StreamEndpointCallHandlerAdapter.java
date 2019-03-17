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
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

public class StreamEndpointCallHandlerAdapter<T> implements EndpointCallHandlerAdapter<Stream<T>, Collection<T>, Collection<T>> {

	private static final StreamEndpointCallHandlerAdapter<Object> DEFAULT_INSTANCE = new StreamEndpointCallHandlerAdapter<>();

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Stream.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return collectionTypeOf(endpointMethod.returnType());
	}

	private JavaType collectionTypeOf(JavaType type) {
		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;
		return JavaType.of(new SimpleParameterizedType(Collection.class, null, responseType));
	}

	@Override
	public EndpointCallHandler<Stream<T>, Collection<T>> adapt(EndpointMethod endpointMethod, EndpointCallHandler<Collection<T>, Collection<T>> delegate) {
		return new StreamEndpointCallHandler(delegate);
	}

	private class StreamEndpointCallHandler implements EndpointCallHandler<Stream<T>, Collection<T>> {

		private final EndpointCallHandler<Collection<T>, Collection<T>> delegate;

		public StreamEndpointCallHandler(EndpointCallHandler<Collection<T>, Collection<T>> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Stream<T> handle(EndpointCall<Collection<T>> call, Object[] args) {
			return Optional.ofNullable(delegate.handle(call, args))
					.map(Collection::stream)
						.orElseGet(Stream::empty);
		}
	}

	public static StreamEndpointCallHandlerAdapter<Object> instance() {
		return DEFAULT_INSTANCE;
	}
}
