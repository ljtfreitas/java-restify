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

import com.github.ljtfreitas.restify.http.client.HttpException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.HeaderParameterResolver;
import com.github.ljtfreitas.restify.reflection.JavaType;

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

			Object body = bodyOf(endpointMethod, args);

			Headers headers = headersOf(endpointMethod, args);

			EndpointVersion version = endpointMethod.version().map(EndpointVersion::of).orElse(null);

			EndpointRequestMetadata metadata = new EndpointRequestMetadata(endpointMethod.metadata().all());

			return new EndpointRequest(endpoint, endpointMethod.httpMethod(), headers, body, responseType, version, metadata);

		} catch (URISyntaxException e) {
			throw new HttpException(e);
		}
	}

	private Object bodyOf(EndpointMethod endpointMethod, Object[] args) {
		return endpointMethod.parameters()
				.ofBody()
					.map(p -> args[p.position()]).orElse(null);
	}

	private Headers headersOf(EndpointMethod endpointMethod, Object[] args) {
		return endpointMethod.headers().all().stream()
				.map(h -> new Header(h.name(),
						new HeaderParameterResolver(h.value(), endpointMethod.parameters()).resolve(args)))
				.reduce(new Headers(), (a, b) -> a.add(b), (a, b) -> b);
	}
}
