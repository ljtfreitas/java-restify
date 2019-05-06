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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageException;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EmptyOnNotFoundEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;

public class JerseyAsyncHttpClientEndpointRequestExecutor implements AsyncEndpointRequestExecutor {

	private final InvocationBuilder invocationBuilder;
	private final EndpointResponseConverter endpointResponseConverter;

	public JerseyAsyncHttpClientEndpointRequestExecutor() {
		this(ClientBuilder.newClient());
	}

	public JerseyAsyncHttpClientEndpointRequestExecutor(EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this(ClientBuilder.newClient(), endpointResponseErrorFallback);
	}

	public JerseyAsyncHttpClientEndpointRequestExecutor(Configuration configuration) {
		this(ClientBuilder.newClient(configuration));
	}

	public JerseyAsyncHttpClientEndpointRequestExecutor(Configuration configuration, EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this(ClientBuilder.newClient(configuration), endpointResponseErrorFallback);
	}

	public JerseyAsyncHttpClientEndpointRequestExecutor(Client client) {
		this(client, new EmptyOnNotFoundEndpointResponseErrorFallback());
	}

	public JerseyAsyncHttpClientEndpointRequestExecutor(Client client, EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this.invocationBuilder = new InvocationBuilder(client);
		this.endpointResponseConverter = new EndpointResponseConverter(endpointResponseErrorFallback);
	}

	@Override
	public <T> CompletionStage<EndpointResponse<T>> executeAsync(EndpointRequest endpointRequest) {
		CompletionStage<Response> stage = invocationBuilder.of(endpointRequest).rx();
		return stage.<EndpointResponse<T>> thenApplyAsync(response -> endpointResponseConverter.convert(response, endpointRequest))
						.whenCompleteAsync((response, e) -> doHandle(response, e, endpointRequest));
	}

	private <T> void doHandle(EndpointResponse<T> response, Throwable throwable, EndpointRequest endpointRequest) {
		if (throwable != null) {
			if (throwable instanceof HttpClientException) {
				throw (HttpClientException) throwable;

			} else if (throwable instanceof HttpMessageException) {
	                throw (HttpMessageException) throwable;

			} else if (throwable instanceof WebApplicationException) {
				throw new HttpMessageReadException(throwable);

			} else {
				throw new HttpClientException("Error on HTTP request: [" + endpointRequest.method() + " " + endpointRequest.endpoint() + "]", throwable);
			}
		}
	}
}
