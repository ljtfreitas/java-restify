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

import java.util.concurrent.CompletionStage;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.RxInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;

class InvocationBuilder {

	private final Client client;

	InvocationBuilder(Client client) {
		this.client = client;
	}

	InvocationBuilderUsingRequest of(EndpointRequest endpointRequest) {
		WebTarget target = client.target(endpointRequest.endpoint());

		Invocation.Builder builder = target.request();

		endpointRequest.headers().all()
			.forEach(header -> builder.header(header.name(), header.value()));

		Entity<Object> entity = endpointRequest.body()
			.map(body -> entity(body, endpointRequest.headers()))
				.orElse(null);


		return new InvocationBuilderUsingRequest(builder, endpointRequest, entity);
	}

	class InvocationBuilderUsingRequest {

		private final Invocation.Builder builder;
		private final EndpointRequest endpointRequest;
		private final Entity<Object> entity;

		InvocationBuilderUsingRequest(Invocation.Builder builder, EndpointRequest endpointRequest, Entity<Object> entity) {
			this.builder = builder;
			this.endpointRequest = endpointRequest;
			this.entity = entity;
		}

		Invocation build() {
			return builder.build(endpointRequest.method(), entity);
		}

		public CompletionStage<Response> rx() {
			return builder.rx().method(endpointRequest.method(), entity);
		}

		public <T, I extends RxInvoker<T>> T rx(Class<I> invokerType) {
			return builder.rx(invokerType).method(endpointRequest.method(), entity);
		}
	}

	private Entity<Object> entity(Object body, Headers headers) {
		String contentType =  headers.get(Headers.CONTENT_TYPE).map(Header::value)
				.orElseThrow(() ->  new HttpMessageWriteException("Your request has a body, but the header [Content-Type] "
						+ "it was not provided."));

		return Entity.entity(body, MediaType.valueOf(contentType));
	}
}
