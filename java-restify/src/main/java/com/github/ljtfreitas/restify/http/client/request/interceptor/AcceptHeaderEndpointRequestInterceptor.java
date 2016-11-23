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
package com.github.ljtfreitas.restify.http.client.request.interceptor;

import static com.github.ljtfreitas.restify.http.client.Headers.ACCEPT;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.Header;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.contract.ContentType;

public class AcceptHeaderEndpointRequestInterceptor implements EndpointRequestInterceptor {

	private Collection<ContentType> contentTypes = new LinkedHashSet<>();

	public AcceptHeaderEndpointRequestInterceptor(String...contentTypes) {
		this.contentTypes.addAll(Arrays.stream(contentTypes).map(ContentType::of).collect(Collectors.toSet()));
	}

	public AcceptHeaderEndpointRequestInterceptor(ContentType...contentTypes) {
		this.contentTypes.addAll(Arrays.stream(contentTypes).collect(Collectors.toSet()));
	}

	@Override
	public EndpointRequest intercepts(EndpointRequest endpointRequest) {
		Optional<Header> accept = endpointRequest.headers().get(ACCEPT);

		if (!accept.isPresent()) {
			String acceptTypes = contentTypes.stream().map(ContentType::name).collect(Collectors.joining(", "));
			endpointRequest.headers().add(new Header(ACCEPT, acceptTypes));
		}

		return endpointRequest;
	}
}
