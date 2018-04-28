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

import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.client.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;

public class RibbonHttpClientRequestFactory implements HttpClientRequestFactory {

	private final DefaultRibbonLoadBalancedClient ribbonLoadBalancedClient;
	private final Charset charset;

	public RibbonHttpClientRequestFactory(ILoadBalancer loadBalancer) {
		this(loadBalancer, new DefaultClientConfigImpl());
	}

	public RibbonHttpClientRequestFactory(ILoadBalancer loadBalancer, IClientConfig clientConfig) {
		this(loadBalancer, clientConfig, new JdkHttpClientRequestFactory());
	}

	public RibbonHttpClientRequestFactory(ILoadBalancer loadBalancer, HttpClientRequestFactory delegate) {
		this(loadBalancer, new DefaultClientConfigImpl(), delegate);
	}

	public RibbonHttpClientRequestFactory(ILoadBalancer loadBalancer, IClientConfig clientConfig, HttpClientRequestFactory delegate) {
		this(loadBalancer, clientConfig, delegate, Encoding.UTF_8.charset());
	}

	public RibbonHttpClientRequestFactory(ILoadBalancer loadBalancer, RibbonExceptionHandler ribbonExceptionHandler) {
		this(loadBalancer, new DefaultClientConfigImpl(), ribbonExceptionHandler);
	}

	public RibbonHttpClientRequestFactory(ILoadBalancer loadBalancer, IClientConfig clientConfig, RibbonExceptionHandler ribbonExceptionHandler) {
		this(loadBalancer, clientConfig, new JdkHttpClientRequestFactory(), ribbonExceptionHandler);
	}

	public RibbonHttpClientRequestFactory(ILoadBalancer loadBalancer, IClientConfig clientConfig, HttpClientRequestFactory delegate,
			RibbonExceptionHandler ribbonExceptionHandler) {
		this(new DefaultRibbonLoadBalancedClient(loadBalancer, clientConfig, delegate, ribbonExceptionHandler), Encoding.UTF_8.charset());
	}

	public RibbonHttpClientRequestFactory(ILoadBalancer loadBalancer, IClientConfig clientConfig, RibbonExceptionHandler ribbonExceptionHandler,
			Charset charset) {
		this(loadBalancer, clientConfig, new JdkHttpClientRequestFactory(), ribbonExceptionHandler, charset);
	}

	public RibbonHttpClientRequestFactory(ILoadBalancer loadBalancer, IClientConfig clientConfig, HttpClientRequestFactory delegate, Charset charset) {
		this(new DefaultRibbonLoadBalancedClient(loadBalancer, clientConfig, delegate), charset);
	}

	public RibbonHttpClientRequestFactory(ILoadBalancer loadBalancer, IClientConfig clientConfig, HttpClientRequestFactory delegate,
			RibbonExceptionHandler ribbonExceptionHandler, Charset charset) {
		this(new DefaultRibbonLoadBalancedClient(loadBalancer, clientConfig, delegate, ribbonExceptionHandler), charset);
	}

	public RibbonHttpClientRequestFactory(DefaultRibbonLoadBalancedClient ribbonLoadBalancedClient) {
		this(ribbonLoadBalancedClient, Encoding.UTF_8.charset());
	}

	public RibbonHttpClientRequestFactory(DefaultRibbonLoadBalancedClient ribbonLoadBalancedClient, Charset charset) {
		this.ribbonLoadBalancedClient = ribbonLoadBalancedClient;
		this.charset = charset;
	}

	@Override
	public HttpClientRequest createOf(EndpointRequest endpointRequest) {
		return new DefaultRibbonHttpClientRequest(endpointRequest, ribbonLoadBalancedClient, charset);
	}
}
