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
package com.github.ljtfreitas.restify.http.client.call.exec.jdk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

public class IteratorEndpointCallExecutableFactory<T> implements EndpointCallExecutableDecoratorFactory<Iterator<T>, Collection<T>, Collection<T>> {

	private static final IteratorEndpointCallExecutableFactory<Object> DEFAULT_INSTANCE = new IteratorEndpointCallExecutableFactory<>();

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Iterator.class);
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
	public EndpointCallExecutable<Iterator<T>, Collection<T>> create(EndpointMethod endpointMethod, EndpointCallExecutable<Collection<T>, Collection<T>> delegate) {
		return new IteratorEndpointCallExecutable(delegate);
	}

	private class IteratorEndpointCallExecutable implements EndpointCallExecutable<Iterator<T>, Collection<T>> {

		private final EndpointCallExecutable<Collection<T>, Collection<T>> delegate;

		public IteratorEndpointCallExecutable(EndpointCallExecutable<Collection<T>, Collection<T>> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Iterator<T> execute(EndpointCall<Collection<T>> call, Object[] args) {
			return Optional.ofNullable(delegate.execute(call, args))
				.map(c -> c.iterator())
					.orElseGet(Collections::emptyIterator);
		}
	}

	public static IteratorEndpointCallExecutableFactory<Object> instance() {
		return DEFAULT_INSTANCE;
	}
}
