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

import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonExceptionHandler;
import com.github.ljtfreitas.restify.http.netflix.client.request.DefaultRibbonHttpClientRequest;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;

public class AsyncRibbonHttpClientRequestFactory implements AsyncHttpClientRequestFactory {

	private final AsyncRibbonLoadBalancedClient asyncRibbonLoadBalancedClient;
	private final Charset charset;

	public AsyncRibbonHttpClientRequestFactory(AsyncHttpClientRequestFactory delegate, ILoadBalancer loadBalancer) {
		this(delegate, loadBalancer, new DefaultClientConfigImpl());
	}

	public AsyncRibbonHttpClientRequestFactory(AsyncHttpClientRequestFactory delegate, ILoadBalancer loadBalancer,
			IClientConfig clientConfig) {
		this(delegate, loadBalancer, clientConfig, Encoding.UTF_8.charset());
	}

	public AsyncRibbonHttpClientRequestFactory(AsyncHttpClientRequestFactory delegate, ILoadBalancer loadBalancer,
			RibbonExceptionHandler ribbonExceptionHandler) {
		this(delegate, loadBalancer, new DefaultClientConfigImpl(), ribbonExceptionHandler);
	}

	public AsyncRibbonHttpClientRequestFactory(AsyncHttpClientRequestFactory delegate, ILoadBalancer loadBalancer,
			IClientConfig clientConfig, RibbonExceptionHandler ribbonExceptionHandler) {
		this(delegate, loadBalancer, clientConfig, ribbonExceptionHandler, Encoding.UTF_8.charset());
	}

	public AsyncRibbonHttpClientRequestFactory(AsyncHttpClientRequestFactory delegate, ILoadBalancer loadBalancer,
			IClientConfig clientConfig, Charset charset) {
		this(new AsyncRibbonLoadBalancedClient(loadBalancer, clientConfig, delegate), charset);
	}

	public AsyncRibbonHttpClientRequestFactory(AsyncHttpClientRequestFactory delegate, ILoadBalancer loadBalancer,
			IClientConfig clientConfig, RibbonExceptionHandler ribbonExceptionHandler, Charset charset) {
		this(new AsyncRibbonLoadBalancedClient(loadBalancer, clientConfig, delegate, ribbonExceptionHandler), charset);
	}

	public AsyncRibbonHttpClientRequestFactory(AsyncRibbonLoadBalancedClient asyncRibbonLoadBalancedClient, Charset charset) {
		this.asyncRibbonLoadBalancedClient = asyncRibbonLoadBalancedClient;
		this.charset = charset;
	}

	@Override
	public HttpClientRequest createOf(EndpointRequest endpointRequest) {
		return new DefaultRibbonHttpClientRequest(endpointRequest, asyncRibbonLoadBalancedClient, charset);
	}

	@Override
	public AsyncHttpClientRequest createAsyncOf(EndpointRequest endpointRequest) {
		return new AsyncRibbonHttpClientRequest(endpointRequest, asyncRibbonLoadBalancedClient, charset);
	}
}
