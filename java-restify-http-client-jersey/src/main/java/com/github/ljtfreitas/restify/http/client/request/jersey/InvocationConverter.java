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
package com.github.ljtfreitas.restify.http.client.request.jersey;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;

class InvocationConverter {

	private final Client client;

	public InvocationConverter(Client client) {
		this.client = client;
	}

	public Invocation convert(EndpointRequest endpointRequest) {
		WebTarget target = client.target(endpointRequest.endpoint());

		Invocation.Builder builder = target.request();

		endpointRequest.headers().all()
			.forEach(header -> builder.header(header.name(), header.value()));

		Entity<Object> entity = endpointRequest.body()
			.map(body -> entity(body, endpointRequest.headers()))
				.orElse(null);

		Invocation invocation = builder.build(endpointRequest.method(), entity);

		return invocation;
	}

	private Entity<Object> entity(Object body, Headers headers) {
		String contentType =  headers.get(Headers.CONTENT_TYPE).map(Header::value)
				.orElseThrow(() ->  new HttpMessageWriteException("Your request has a body, but the header [Content-Type] "
						+ "it was not provided."));

		return Entity.entity(body, MediaType.valueOf(contentType));
	}
}
