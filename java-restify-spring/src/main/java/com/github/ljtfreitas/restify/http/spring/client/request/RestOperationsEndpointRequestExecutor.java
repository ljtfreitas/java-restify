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
package com.github.ljtfreitas.restify.http.spring.client.request;

import java.lang.reflect.Type;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;

public class RestOperationsEndpointRequestExecutor implements EndpointRequestExecutor {

	private final RestOperations rest;
	private final RequestEntityConverter requestEntityConverter;
	private final EndpointResponseConverter responseEntityConverter;

	public RestOperationsEndpointRequestExecutor(RestOperations rest) {
		this(rest, new RequestEntityConverter(), new EndpointResponseConverter());
	}

	public RestOperationsEndpointRequestExecutor(RestOperations rest, RequestEntityConverter requestEntityConverter,
			EndpointResponseConverter responseEntityConverter) {
		this.rest = rest;
		this.requestEntityConverter = requestEntityConverter;
		this.responseEntityConverter = responseEntityConverter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EndpointResponse<T> execute(EndpointRequest endpointRequest) {
		RequestEntity<Object> request = requestEntityConverter.convert(endpointRequest);

		ResponseEntity<Object> response = rest.exchange(request, new JavaTypeReference(endpointRequest.responseType()));

		return (EndpointResponse<T>) responseEntityConverter.convert(response);
	}

	private class JavaTypeReference extends ParameterizedTypeReference<Object> {

		private final JavaType type;

		public JavaTypeReference(JavaType type) {
			this.type = type;
		}

		@Override
		public Type getType() {
			return type.unwrap();
		}

		@Override
		public boolean equals(Object obj) {
			return (this == obj || (obj instanceof ParameterizedTypeReference
					&& type.equals(((ParameterizedTypeReference<?>) obj).getType())));
		}

		@Override
		public int hashCode() {
			return type.hashCode();
		}

		@Override
		public String toString() {
			return "JavaTypeReference<" + type + ">";
		}
	}
}
