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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class CollectionEndpointCallHandlerFactory<T> implements EndpointCallHandlerFactory<Collection<T>, Collection<T>> {

	private static final CollectionEndpointCallHandlerFactory<Object> DEFAULT_INSTANCE = new CollectionEndpointCallHandlerFactory<>();

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		Class<?> classType = endpointMethod.returnType().classType();
		return List.class.isAssignableFrom(classType)
			|| Set.class.isAssignableFrom(classType)
			|| Collection.class.equals(classType);
	}

	@Override
	public EndpointCallHandler<Collection<T>, Collection<T>> create(EndpointMethod endpointMethod) {
		return new CollectionEndpointCallHandler(endpointMethod.returnType());
	}

	private class CollectionEndpointCallHandler implements EndpointCallHandler<Collection<T>, Collection<T>> {

		private final JavaType returnType;

		public CollectionEndpointCallHandler(JavaType returnType) {
			this.returnType = returnType;
		}

		@Override
		public JavaType returnType() {
			return returnType;
		}

		@Override
		public Collection<T> handle(EndpointCall<Collection<T>> call, Object[] args) {
			return Optional.ofNullable(call.execute()).orElseGet(() -> empty());
		}

		private Collection<T> empty() {
			Class<?> classType = returnType.classType();

			if (classType.equals(NavigableSet.class)) {
				return Collections.emptyNavigableSet();

			} else if (classType.equals(SortedSet.class)) {
				return Collections.emptySortedSet();

			} else if (Set.class.isAssignableFrom(classType)) {
				return Collections.emptySet();

			} else {
				return Collections.emptyList();
			}
		}
	}

	public static CollectionEndpointCallHandlerFactory<Object> instance() {
		return DEFAULT_INSTANCE;
	}
}
