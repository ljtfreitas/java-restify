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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestOperations;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.HttpException;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.DefaultEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class RestOperationsEndpointRequestExecutor implements EndpointRequestExecutor {

	private final RestOperations rest;
	private final EndpointResponseErrorFallback endpointResponseErrorFallback;
	private final RequestEntityConverter requestEntityConverter;
	private final EndpointResponseConverter responseEntityConverter;

	public RestOperationsEndpointRequestExecutor(RestOperations rest) {
		this(rest, new DefaultEndpointResponseErrorFallback());
	}

	public RestOperationsEndpointRequestExecutor(RestOperations rest, EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this(rest, new RequestEntityConverter(), new EndpointResponseConverter(), endpointResponseErrorFallback);
	}

	public RestOperationsEndpointRequestExecutor(RestOperations rest, RequestEntityConverter requestEntityConverter,
			EndpointResponseConverter responseEntityConverter, EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this.rest = rest;
		this.requestEntityConverter = requestEntityConverter;
		this.responseEntityConverter = responseEntityConverter;
		this.endpointResponseErrorFallback = endpointResponseErrorFallback;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EndpointResponse<T> execute(EndpointRequest endpointRequest) {
		RequestEntity<Object> request = requestEntityConverter.convert(endpointRequest);

		try {
			ResponseEntity<Object> response = rest.exchange(request, new JavaTypeReference(endpointRequest.responseType()));

			return (EndpointResponse<T>) responseEntityConverter.convert(response);

		} catch (RestClientResponseException e) {
			return endpointResponseErrorFallback.onError(ErrorHttpResponseMessage.from(request, e), endpointRequest.responseType());

		} catch (ResourceAccessException e) {
			throw new HttpClientException("I/O error on HTTP request: [" + request.getMethod() + " " + request.getUrl() + "]", e);

		} catch (Exception e) {
			throw new HttpException("Error on HTTP request: [" + request.getMethod() + " " + request.getUrl() + "]", e);
		}
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
			if ((obj instanceof JavaTypeReference)) return false;
			JavaTypeReference that = (JavaTypeReference) obj;
			return this == that || this.type.equals(that.type);
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
