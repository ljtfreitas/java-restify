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
package com.github.ljtfreitas.restify.http.netflix.client.request.async;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonExceptionHandler;
import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonLoadBalancedClient;
import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonRequest;
import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonResponse;
import com.github.ljtfreitas.restify.http.netflix.client.request.SimpleRibbonExceptionHandler;
import com.netflix.client.ClientException;
import com.netflix.client.RetryHandler;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.LoadBalancerContext;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.reactive.LoadBalancerCommand;
import com.netflix.loadbalancer.reactive.ServerOperation;

import rx.Observable;

public class AsyncRibbonLoadBalancedClient implements RibbonLoadBalancedClient {

	private final LoadBalancerContext loadBalancerContext;
	private final AsyncHttpClientRequestFactory asyncHttpClientRequestFactory;
	private final RibbonExceptionHandler ribbonExceptionHandler;

	public AsyncRibbonLoadBalancedClient(ILoadBalancer loadBalancer, IClientConfig clientConfig,
			AsyncHttpClientRequestFactory asyncHttpClientRequestFactory) {
		this(loadBalancer, clientConfig, asyncHttpClientRequestFactory, new SimpleRibbonExceptionHandler());
	}

	public AsyncRibbonLoadBalancedClient(ILoadBalancer loadBalancer, IClientConfig clientConfig,
			AsyncHttpClientRequestFactory asyncHttpClientRequestFactory, RibbonExceptionHandler ribbonExceptionHandler) {
		this(new LoadBalancerContext(loadBalancer, clientConfig), asyncHttpClientRequestFactory, ribbonExceptionHandler);
	}

	public AsyncRibbonLoadBalancedClient(LoadBalancerContext loadBalancerContext, AsyncHttpClientRequestFactory asyncHttpClientRequestFactory,
			RibbonExceptionHandler ribbonExceptionHandler) {
		this.loadBalancerContext = loadBalancerContext;
		this.asyncHttpClientRequestFactory = asyncHttpClientRequestFactory;
		this.ribbonExceptionHandler = ribbonExceptionHandler;
	}

	@Override
	public RibbonResponse withLoadBalancer(RibbonRequest request) throws ClientException {
		return doExecuteAsync(request).join();
	}

	public CompletableFuture<RibbonResponse> executeAsync(RibbonRequest request) {
		return doExecuteAsync(request);
	}

	private CompletableFuture<RibbonResponse> doExecuteAsync(RibbonRequest request) {
		LoadBalancerCommand<RibbonResponse> command = LoadBalancerCommand.<RibbonResponse> builder()
				.withLoadBalancerContext(loadBalancerContext)
				.withRetryHandler(retryHandler())
				.withLoadBalancerURI(request.getUri()).build();

		CompletableFuture<RibbonResponse> future = new CompletableFuture<>();

		command.submit(new LoadBalancedRequest(request))
			.doOnError(future::completeExceptionally)
				.single()
					.forEach(future::complete);

		return future;
	}

	private RetryHandler retryHandler() {
		return null;
	}

	private class LoadBalancedRequest implements ServerOperation<RibbonResponse> {

		private final RibbonRequest request;

		private LoadBalancedRequest(RibbonRequest request) {
			this.request = request;
		}

		@Override
		public Observable<RibbonResponse> call(Server server) {
			URI finalUri = loadBalancerContext.reconstructURIWithServer(server, request.getUri());
			RibbonRequest newRequest = request.replaceUri(finalUri);

			AsyncHttpClientRequest asyncRibbonHttpRequestMessage = asyncHttpClientRequestFactory
					.createAsyncOf(newRequest.endpointRequest());
			request.writeTo(asyncRibbonHttpRequestMessage);

			CompletableFuture<HttpResponseMessage> httpResponseAsFuture = asyncRibbonHttpRequestMessage.executeAsync();

			return Observable.from(httpResponseAsFuture).map(RibbonResponse::new).onErrorResumeNext(this::onError);
		}

		private Observable<RibbonResponse> onError(Throwable e) {
			ribbonExceptionHandler.onException(request, e);
			return e.getCause() instanceof ClientException ?
					Observable.error((ClientException) e.getCause()) :
						Observable.error(new ClientException(e));
		}
	}
}
