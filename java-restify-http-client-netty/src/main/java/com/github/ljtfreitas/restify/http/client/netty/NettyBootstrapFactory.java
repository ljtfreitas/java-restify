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

import java.util.concurrent.TimeUnit;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.Timeout;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.ReadTimeoutHandler;

class NettyBootstrapFactory {

	private final EventLoopGroup eventLoopGroup;
	private final NettyHttpClientRequestConfiguration nettyHttpClientRequestConfiguration;

	public NettyBootstrapFactory(EventLoopGroup eventLoopGroup, NettyHttpClientRequestConfiguration nettyHttpClientRequestConfiguration) {
		this.eventLoopGroup = eventLoopGroup;
		this.nettyHttpClientRequestConfiguration = nettyHttpClientRequestConfiguration;
	}

	public Bootstrap createTo(EndpointRequest source) {
		Bootstrap bootstrap = new Bootstrap();

		bootstrap.group(this.eventLoopGroup)
			.channel(NioSocketChannel.class)
				.handler(new NettyChannelInitializer(source));

		return bootstrap;
	}

	private class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {

		private final EndpointRequest source;

		private NettyChannelInitializer(EndpointRequest source) {
			this.source = source;
		}

		@Override
		protected void initChannel(SocketChannel channel) throws Exception {
			configure(channel.config());
			ChannelPipeline pipeline = channel.pipeline();

			nettyHttpClientRequestConfiguration.sslContext().ifPresent(sslContext -> pipeline.addLast(sslContext.newHandler(channel.alloc())));

			pipeline.addLast(new HttpClientCodec());
			pipeline.addLast(new HttpObjectAggregator(nettyHttpClientRequestConfiguration.maxResponseSize()));

			long readTimeout = source.metadata().get(Timeout.class)
					.map(t -> t.read())
						.filter(t -> t >= 0)
							.orElse(nettyHttpClientRequestConfiguration.readTimeout());
			pipeline.addLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS));
		}

		private void configure(SocketChannelConfig channelConfiguration) {
			int connectionTimeout = source.metadata().get(Timeout.class)
					.map(t -> (int) t.connection())
						.filter(t -> t >= 0)
							.orElse(nettyHttpClientRequestConfiguration.connectionTimeout());

			channelConfiguration.setConnectTimeoutMillis(connectionTimeout);
		}
	}
}
