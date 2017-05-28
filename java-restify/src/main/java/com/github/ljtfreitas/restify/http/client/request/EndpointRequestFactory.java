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
package com.github.ljtfreitas.restify.http.client.request;

import java.net.URI;
import java.net.URISyntaxException;

import com.github.ljtfreitas.restify.http.RestifyHttpException;
import com.github.ljtfreitas.restify.http.client.Header;
import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointHeaderParameterResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;

public class EndpointRequestFactory {

	private final EndpointRequestInterceptorStack interceptors;

	public EndpointRequestFactory(EndpointRequestInterceptorStack interceptors) {
		this.interceptors = interceptors;
	}

	public EndpointRequest createWith(EndpointMethod endpointMethod, Object[] args) {
		return interceptors.apply(newRequest(endpointMethod, args, endpointMethod.returnType()));
	}

	public EndpointRequest createWith(EndpointMethod endpointMethod, Object[] args, JavaType responseType) {
		return interceptors.apply(newRequest(endpointMethod, args, responseType));
	}

	private EndpointRequest newRequest(EndpointMethod endpointMethod, Object[] args, JavaType responseType) {
		try {
			URI endpoint = new URI(endpointMethod.expand(args));

			Object body = endpointMethod.parameters()
					.ofBody()
						.map(p -> args[p.position()]).orElse(null);

			Headers headers = new Headers();
			endpointMethod.headers().all().stream()
				.forEach(h -> headers.add(new Header(h.name(), new EndpointHeaderParameterResolver(h.value(), endpointMethod.parameters())
						.resolve(args))));

			EndpointVersion version = endpointMethod.version().map(EndpointVersion::of).orElse(null);

			return new EndpointRequest(endpoint, endpointMethod.httpMethod(), headers, body, responseType, version);

		} catch (URISyntaxException e) {
			throw new RestifyHttpException(e);
		}
	}
}
