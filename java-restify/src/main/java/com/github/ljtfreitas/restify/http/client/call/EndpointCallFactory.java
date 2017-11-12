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
package com.github.ljtfreitas.restify.http.client.call;

import java.lang.reflect.ParameterizedType;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestFactory;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class EndpointCallFactory {

	private final EndpointRequestFactory endpointRequestFactory;
	private final EndpointRequestExecutor endpointRequestExecutor;

	public EndpointCallFactory(EndpointRequestFactory endpointRequestFactory,
			EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequestFactory = endpointRequestFactory;
		this.endpointRequestExecutor = endpointRequestExecutor;
	}

	public <T> EndpointCall<T> createWith(EndpointMethod endpointMethod, Object[] args, JavaType returnType) {
		return doCreate(endpointMethod, args, returnType);
	}

	private <T> EndpointCall<T> doCreate(EndpointMethod endpointMethod, Object[] args, JavaType returnType) {
		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args, rawTypeOf(returnType));

		if (returnType.is(EndpointResponse.class)) {
			return endpointResponseCall(endpointRequest);

		} else {
			return new DefaultEndpointCall<>(endpointRequest, endpointRequestExecutor);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> EndpointCall<T> endpointResponseCall(EndpointRequest endpointRequest) {
		return (EndpointCall<T>) new EndpointResponseCall<>(endpointRequest, endpointRequestExecutor);
	}

	private JavaType rawTypeOf(JavaType returnType) {
		return returnType.is(EndpointResponse.class) ? rawParameterizedTypeOf(returnType) : returnType;
	}

	private JavaType rawParameterizedTypeOf(JavaType returnType) {
		return JavaType.of(returnType.parameterized() ? returnType.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class);
	}
}
