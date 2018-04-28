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

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutables;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestFactory;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class EndpointMethodExecutor {

	private final EndpointRequestFactory endpointRequestFactory;
	private final EndpointCallExecutables endpointCallExecutables;
	private final EndpointCallFactory endpointCallFactory;

	public EndpointMethodExecutor(EndpointRequestFactory endpointRequestFactory, EndpointCallExecutables endpointCallExecutables,
			EndpointCallFactory endpointCallFactory) {
		this.endpointRequestFactory = endpointRequestFactory;
		this.endpointCallExecutables = endpointCallExecutables;
		this.endpointCallFactory = endpointCallFactory;
	}

	public Object execute(EndpointMethod endpointMethod, Object[] args) {
		EndpointCallExecutable<Object, Object> executable = endpointCallExecutables.of(endpointMethod);

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args, rawTypeOf(executable.returnType()));

		EndpointCall<Object> call = endpointCallFactory.createWith(endpointRequest, executable.returnType());

		return executable.execute(call, args);
	}

	private JavaType rawTypeOf(JavaType returnType) {
		return returnType.is(EndpointResponse.class) ? rawParameterizedTypeOf(returnType) : returnType;
	}

	private JavaType rawParameterizedTypeOf(JavaType returnType) {
		return JavaType.of(returnType.parameterized() ? returnType.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class);
	}
}
