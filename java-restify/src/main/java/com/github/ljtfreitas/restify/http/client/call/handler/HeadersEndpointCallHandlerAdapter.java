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
package com.github.ljtfreitas.restify.http.client.call.handler;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

public class HeadersEndpointCallHandlerAdapter implements EndpointCallHandlerAdapter<Headers, EndpointResponse<Void>, Void> {

	private static final HeadersEndpointCallHandlerAdapter DEFAULT_INSTANCE = new HeadersEndpointCallHandlerAdapter();

	private static final JavaType DEFAULT_RETURN_TYPE = JavaType.of(new SimpleParameterizedType(EndpointResponse.class, null, Void.class));

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Headers.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return DEFAULT_RETURN_TYPE;
	}

	@Override
	public EndpointCallHandler<Headers, Void> adapt(EndpointMethod endpointMethod, EndpointCallHandler<EndpointResponse<Void>, Void> handler) {
		return new HeadersEndpointCallHandler(handler);
	}

	private class HeadersEndpointCallHandler implements EndpointCallHandler<Headers, Void> {

		private final EndpointCallHandler<EndpointResponse<Void>, Void> delegate;

		public HeadersEndpointCallHandler(EndpointCallHandler<EndpointResponse<Void>, Void> handler) {
			this.delegate = handler;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Headers handle(EndpointCall<Void> call, Object[] args) {
			return delegate.handle(call, args).headers();
		}
	}

	public static HeadersEndpointCallHandlerAdapter instance() {
		return DEFAULT_INSTANCE;
	}
}
