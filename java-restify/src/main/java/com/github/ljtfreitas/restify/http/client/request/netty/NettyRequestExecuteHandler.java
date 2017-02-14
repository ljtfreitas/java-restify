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
package com.github.ljtfreitas.restify.http.client.request.netty;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import com.github.ljtfreitas.restify.http.client.Header;
import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;

import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;

class NettyRequestExecuteHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

	private final CompletableFuture<NettyHttpClientResponse> future;
	private final HttpRequestMessage source;

	public NettyRequestExecuteHandler(CompletableFuture<NettyHttpClientResponse> future, NettyHttpClientRequest source) {
		this.future = future;
		this.source = source;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext context, FullHttpResponse nettyResponse) throws Exception {
		NettyHttpClientResponse nettyHttpClientResponse = convert(context, nettyResponse);

		future.complete(nettyHttpClientResponse);
	}

	private NettyHttpClientResponse convert(ChannelHandlerContext context, FullHttpResponse nettyResponse) {
		nettyResponse.retain();

		StatusCode statusCode = StatusCode.of(nettyResponse.getStatus().code());

		Headers headers = headersOf(nettyResponse);

		InputStream body = new ByteBufInputStream(nettyResponse.content());

		NettyHttpClientResponse nettyHttpClientResponse = new NettyHttpClientResponse(statusCode, headers, body, source,
				context, nettyResponse);

		return nettyHttpClientResponse;
	}

	private Headers headersOf(FullHttpResponse nettyResponse) {
		Headers headers = new Headers();

		nettyResponse.headers().entries().stream()
			.map(header -> new Header(header.getKey(), header.getValue()))
				.forEach(headers::add);

		return headers;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
		future.completeExceptionally(cause);
	}
}
