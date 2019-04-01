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
package com.github.ljtfreitas.restify.http.client.netty;

import java.io.Closeable;
import java.io.IOException;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;

public class NettyHttpClientRequestFactory implements AsyncHttpClientRequestFactory, Closeable {

	private final EventLoopGroup eventLoopGroup;
	private final NettyHttpClientRequestConfiguration nettyHttpClientRequestConfiguration;

	public NettyHttpClientRequestFactory() {
		this(new NettyEventLoopGroupFactory().create());
	}

	public NettyHttpClientRequestFactory(EventLoopGroup eventLoopGroup) {
		this(eventLoopGroup, NettyHttpClientRequestConfiguration.useDefault());
	}

	public NettyHttpClientRequestFactory(NettyHttpClientRequestConfiguration nettyHttpClientRequestConfiguration) {
		this(new NettyEventLoopGroupFactory().create(), nettyHttpClientRequestConfiguration);
	}

	public NettyHttpClientRequestFactory(EventLoopGroup eventLoopGroup, NettyHttpClientRequestConfiguration nettyHttpClientRequestConfiguration) {
		this.eventLoopGroup = eventLoopGroup;
		this.nettyHttpClientRequestConfiguration = nettyHttpClientRequestConfiguration;
	}

	@Override
	public NettyHttpClientRequest createOf(EndpointRequest endpointRequest) {
		Bootstrap bootstrap = new NettyBootstrapFactory(eventLoopGroup, nettyHttpClientRequestConfiguration)
				.createTo(endpointRequest);

		return new NettyHttpClientRequest(bootstrap, endpointRequest.endpoint(), endpointRequest.headers(), endpointRequest.method(),
				nettyHttpClientRequestConfiguration.charset());
	}

	@Override
	public NettyHttpClientRequest createAsyncOf(EndpointRequest endpointRequest) {
		Bootstrap bootstrap = new NettyBootstrapFactory(eventLoopGroup, nettyHttpClientRequestConfiguration)
				.createTo(endpointRequest);

		return new NettyHttpClientRequest(bootstrap, endpointRequest.endpoint(), endpointRequest.headers(), endpointRequest.method(),
				nettyHttpClientRequestConfiguration.charset());
	}

	@Override
	public void close() throws IOException {
		eventLoopGroup.shutdownGracefully().syncUninterruptibly();
	}

}
