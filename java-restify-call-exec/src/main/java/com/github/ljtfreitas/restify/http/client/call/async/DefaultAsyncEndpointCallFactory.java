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
package com.github.ljtfreitas.restify.http.client.call.async;

import java.util.concurrent.Executor;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.EndpointCallFactory;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class DefaultAsyncEndpointCallFactory implements EndpointCallFactory {

	private final AsyncEndpointRequestExecutor asyncEndpointRequestExecutor;
	private final EndpointCallFactory delegate;
	private final Executor executor;

	public DefaultAsyncEndpointCallFactory(AsyncEndpointRequestExecutor asyncEndpointRequestExecutor, Executor executor, EndpointCallFactory delegate) {
		this.asyncEndpointRequestExecutor = asyncEndpointRequestExecutor;
		this.executor = executor;
		this.delegate = delegate;
	}

	@Override
	public <T> EndpointCall<T> createWith(EndpointRequest endpointRequest, JavaType returnType) {

		if (returnType.is(EndpointResponse.class)) {
			return asyncEndpointResponseCall(endpointRequest);

		} else {
			EndpointCall<T> source = delegate.createWith(endpointRequest, returnType);

			return new DefaultAsyncEndpointCall<>(endpointRequest, asyncEndpointRequestExecutor, executor, source);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> EndpointCall<T> asyncEndpointResponseCall(EndpointRequest endpointRequest) {
		return (EndpointCall<T>) new AsyncEndpointResponseCall<>(endpointRequest, asyncEndpointRequestExecutor, executor);
	}
}
