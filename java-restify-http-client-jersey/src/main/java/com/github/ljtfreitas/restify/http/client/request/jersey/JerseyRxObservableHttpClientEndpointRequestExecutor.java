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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvokerProvider;

import com.github.ljtfreitas.restify.http.client.HttpException;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EmptyOnNotFoundEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;

import rx.Observable;

public class JerseyRxObservableHttpClientEndpointRequestExecutor implements AsyncEndpointRequestExecutor {

	private final InvocationBuilder invocationBuilder;
	private final EndpointResponseConverter endpointResponseConverter;

	public JerseyRxObservableHttpClientEndpointRequestExecutor() {
		this(ClientBuilder.newClient());
	}

	public JerseyRxObservableHttpClientEndpointRequestExecutor(EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this(ClientBuilder.newClient(), endpointResponseErrorFallback);
	}

	public JerseyRxObservableHttpClientEndpointRequestExecutor(Configuration configuration) {
		this(ClientBuilder.newClient(configuration));
	}

	public JerseyRxObservableHttpClientEndpointRequestExecutor(Configuration configuration, EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this(ClientBuilder.newClient(configuration), endpointResponseErrorFallback);
	}

	public JerseyRxObservableHttpClientEndpointRequestExecutor(Client client) {
		this(client, new EmptyOnNotFoundEndpointResponseErrorFallback());
	}

	public JerseyRxObservableHttpClientEndpointRequestExecutor(Client client, EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this.invocationBuilder = new InvocationBuilder(configure(client));
		this.endpointResponseConverter = new EndpointResponseConverter(endpointResponseErrorFallback);
	}

	private Client configure(Client client) {
		return client.register(RxObservableInvokerProvider.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> CompletionStage<EndpointResponse<T>> executeAsync(EndpointRequest endpointRequest) {
		Observable<Response> observable = invocationBuilder.of(endpointRequest).rx(RxObservableInvoker.class);

		CompletableFuture<EndpointResponse<T>> future = new CompletableFuture<>();

		observable
			.<EndpointResponse<T>> map(response -> endpointResponseConverter.convert(response, endpointRequest))
			.onErrorResumeNext(e -> handleException(e, endpointRequest))
				.subscribe(future::complete, future::completeExceptionally);

		return future;
	}

	private <T> Observable<EndpointResponse<T>> handleException(Throwable throwable, EndpointRequest endpointRequest) {
		if (throwable instanceof HttpException) {
			return Observable.error(throwable);

		} else if (throwable instanceof WebApplicationException) {
			return Observable.error(new HttpMessageReadException(throwable));

		} else {
			return Observable.error(new HttpException("Error on HTTP request: [" + endpointRequest.method() + " " + endpointRequest.endpoint() + "]", throwable));
		}
	}
}
