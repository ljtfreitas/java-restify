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
package com.github.ljtfreitas.restify.http.netflix.client.request;

import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.netflix.client.AbstractLoadBalancerAwareClient;
import com.netflix.client.ClientException;
import com.netflix.client.RequestSpecificRetryHandler;
import com.netflix.client.RetryHandler;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.client.config.IClientConfigKey;
import com.netflix.loadbalancer.ILoadBalancer;

public class DefaultRibbonLoadBalancedClient extends AbstractLoadBalancerAwareClient<RibbonRequest, RibbonResponse>
	implements RibbonLoadBalancedClient {

	private final IClientConfig clientConfig;
	private final HttpClientRequestFactory httpClientRequestFactory;
	private final RibbonExceptionHandler ribbonExceptionHandler;

	public DefaultRibbonLoadBalancedClient(ILoadBalancer loadBalancer) {
		this(loadBalancer, new DefaultClientConfigImpl(), new JdkHttpClientRequestFactory(), new SimpleRibbonExceptionHandler());
	}

	public DefaultRibbonLoadBalancedClient(ILoadBalancer loadBalancer, IClientConfig clientConfig) {
		this(loadBalancer, clientConfig, new JdkHttpClientRequestFactory(), new SimpleRibbonExceptionHandler());
	}

	public DefaultRibbonLoadBalancedClient(ILoadBalancer loadBalancer, IClientConfig clientConfig, HttpClientRequestFactory httpClientRequestFactory) {
		this(loadBalancer, clientConfig, httpClientRequestFactory, new SimpleRibbonExceptionHandler());
	}

	public DefaultRibbonLoadBalancedClient(ILoadBalancer loadBalancer, HttpClientRequestFactory httpClientRequestFactory) {
		this(loadBalancer, new DefaultClientConfigImpl(), httpClientRequestFactory, new SimpleRibbonExceptionHandler());
	}

	public DefaultRibbonLoadBalancedClient(ILoadBalancer loadBalancer, RibbonExceptionHandler ribbonExceptionHandler) {
		this(loadBalancer, new DefaultClientConfigImpl(), new JdkHttpClientRequestFactory(), ribbonExceptionHandler);
	}

	public DefaultRibbonLoadBalancedClient(ILoadBalancer loadBalancer, IClientConfig clientConfig, RibbonExceptionHandler ribbonExceptionHandler) {
		this(loadBalancer, clientConfig, new JdkHttpClientRequestFactory(), ribbonExceptionHandler);
	}

	public DefaultRibbonLoadBalancedClient(ILoadBalancer loadBalancer, IClientConfig clientConfig, HttpClientRequestFactory httpClientRequestFactory,
			RibbonExceptionHandler ribbonExceptionHandler) {
		super(loadBalancer, clientConfig);
		this.clientConfig = clientConfig;
		this.httpClientRequestFactory = httpClientRequestFactory;
		this.ribbonExceptionHandler = ribbonExceptionHandler;
	}

	@Override
	public RibbonResponse withLoadBalancer(RibbonRequest request) throws ClientException {
		return super.executeWithLoadBalancer(request);
	}

	@Override
	public RibbonResponse execute(RibbonRequest request, IClientConfig requestConfig) throws Exception {
		try {
			HttpClientRequest ribbonHttpRequestMessage = httpClientRequestFactory.createOf(request.endpointRequest());

			request.writeTo(ribbonHttpRequestMessage);

			HttpResponseMessage httpResponse = ribbonHttpRequestMessage.execute();

			return new RibbonResponse(httpResponse);

		} catch (Exception e) {
			ribbonExceptionHandler.onException(request, e);
			throw e;
		}
	}

	@Override
	public RequestSpecificRetryHandler getRequestSpecificRetryHandler(RibbonRequest request, IClientConfig requestConfig) {
		IClientConfig ribbonRequestConfiguration = Optional.ofNullable(requestConfig).orElse(clientConfig);
		boolean retryAllOperations = ribbonRequestConfiguration.get(IClientConfigKey.Keys.OkToRetryOnAllOperations,
				DefaultClientConfigImpl.DEFAULT_OK_TO_RETRY_ON_ALL_OPERATIONS);

		return new RequestSpecificRetryHandler(true, (retryAllOperations || request.isGet()) , RetryHandler.DEFAULT, ribbonRequestConfiguration);
	}
}
