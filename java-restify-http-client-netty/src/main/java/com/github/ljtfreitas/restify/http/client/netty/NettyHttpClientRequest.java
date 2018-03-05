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

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

class NettyHttpClientRequest implements AsyncHttpClientRequest {

	private static final String HTTP_SCHEME = "http";
	private static final String HTTPS_SCHEME = "https";

	private static final int HTTP_SCHEME_PORT = 80;
	private static final int HTTPS_SCHEME_PORT = 443;
	
	private final Bootstrap bootstrap;
	private final URI uri;
	private final Headers headers;
	private final String method;
	private final Charset charset;
	private final ByteBufOutputStream body;

	public NettyHttpClientRequest(Bootstrap bootstrap, URI uri, Headers headers, String method, Charset charset) {
		this(bootstrap, uri, headers, method, charset, new ByteBufOutputStream(Unpooled.buffer()));
	}

	private NettyHttpClientRequest(Bootstrap bootstrap, URI uri, Headers headers, String method, Charset charset, ByteBufOutputStream body) {
		this.bootstrap = bootstrap;
		this.uri = uri;
		this.headers = headers;
		this.method = method;
		this.charset = charset;
		this.body = body;
	}

	@Override
	public URI uri() {
		return uri;
	}

	@Override
	public String method() {
		return method;
	}

	@Override
	public OutputStream output() {
		return body;
	}

	@Override
	public Headers headers() {
		return headers;
	}

	@Override
	public Charset charset() {
		return charset;
	}

	@Override
	public HttpRequestMessage replace(Header header) {
		return new NettyHttpClientRequest(bootstrap, uri, headers.replace(header), method, charset, body);
	}

	@Override
	public CompletableFuture<HttpResponseMessage> executeAsync() throws HttpClientException {
		return doExecuteAsync();
	}

	@Override
	public HttpResponseMessage execute() throws HttpClientException {
		try {
			return doExecuteAsync().get();

		} catch (InterruptedException | ExecutionException e) {
			throw new HttpClientException("I/O error on HTTP request: [" + method + " " + uri + "]", e);
		}
	}

	private CompletableFuture<HttpResponseMessage> doExecuteAsync() {
		final CompletableFuture<HttpResponseMessage> responseAsFuture = new CompletableFuture<>();

		NettyRequestExecuteHandler nettyRequestExecuteHandler = new NettyRequestExecuteHandler(responseAsFuture, this);

		ChannelFutureListener connectionListener = new NettyChannelFutureListener(nettyHttpRequest(), nettyRequestExecuteHandler);

		bootstrap.connect(uri.getHost(), port())
				.addListener(connectionListener);

		return responseAsFuture;
	}

	private int port() {
		int port = uri.getPort();

		if (port == -1) {
			if (HTTP_SCHEME.equalsIgnoreCase(uri.getScheme())) {
				port = HTTP_SCHEME_PORT;

			} else if (HTTPS_SCHEME.equalsIgnoreCase(uri.getScheme())) {
				port = HTTPS_SCHEME_PORT;
			}
		}

		return port;
	}

	private HttpRequest nettyHttpRequest() {
		HttpMethod nettyMethod = HttpMethod.valueOf(method);

		ByteBuf bodyBuffer = body.buffer();

		FullHttpRequest nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, nettyMethod,
				uri.toString(), bodyBuffer);

		nettyRequest.headers().set(Headers.HOST, uri.getHost());
		nettyRequest.headers().set(Headers.CONNECTION, "close");

		if (bodyBuffer.readableBytes() != 0) {
			nettyRequest.headers().set(Headers.CONTENT_LENGTH, bodyBuffer.readableBytes());
		}

		headers.all().forEach(header -> nettyRequest.headers().add(header.name(), header.value()));

		return nettyRequest;
	}
}
