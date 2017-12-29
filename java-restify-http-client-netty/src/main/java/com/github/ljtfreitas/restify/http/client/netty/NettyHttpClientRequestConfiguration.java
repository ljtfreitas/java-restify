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

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.message.Encoding;

import io.netty.handler.ssl.SslContext;

public class NettyHttpClientRequestConfiguration {

	private static final int DEFAULT_MAX_RESPONSE_SIZE = 1024 * 1024 * 10;

	private int connectionTimeout = 0;
	private int readTimeout = 0;
	private int maxResponseSize = DEFAULT_MAX_RESPONSE_SIZE;

	private SslContext sslContext = null;
	private Charset charset = Encoding.UTF_8.charset();

	private NettyHttpClientRequestConfiguration() {
	}

	private NettyHttpClientRequestConfiguration(NettyHttpClientRequestConfiguration configuration) {
		this.connectionTimeout = configuration.connectionTimeout;
		this.readTimeout = configuration.readTimeout;
		this.maxResponseSize = configuration.maxResponseSize;
		this.sslContext = configuration.sslContext;
		this.charset = configuration.charset;
	}

	public int connectionTimeout() {
		return connectionTimeout;
	}

	public long readTimeout() {
		return readTimeout;
	}

	public int maxResponseSize() {
		return maxResponseSize;
	}

	public Optional<SslContext> sslContext() {
		return Optional.ofNullable(sslContext);
	}

	public Charset charset() {
		return charset;
	}

	public static NettyHttpClientRequestConfiguration useDefault() {
		return new NettyHttpClientRequestConfiguration();
	}

	public static class Builder {

		private NettyHttpClientRequestConfiguration configuration = new NettyHttpClientRequestConfiguration();

		public Builder connectionTimeout(int connectionTimeout) {
			configuration.connectionTimeout = connectionTimeout;
			return this;
		}

		public Builder connectionTimeout(Duration duration) {
			configuration.connectionTimeout = (int) duration.toMillis();
			return this;
		}

		public Builder readTimeout(int readTimeout) {
			configuration.readTimeout = readTimeout;
			return this;
		}

		public Builder readTimeout(Duration duration) {
			configuration.readTimeout = (int) duration.toMillis();
			return this;
		}

		public Builder maxResponseSize(int maxResponseSize) {
			configuration.maxResponseSize = maxResponseSize;
			return this;
		}

		public Builder sslContext(SslContext sslContext) {
			configuration.sslContext = sslContext;
			return this;
		}

		public Builder charset(Charset charset) {
			configuration.charset = charset;
			return this;
		}

		public NettyHttpClientRequestConfiguration build() {
			return new NettyHttpClientRequestConfiguration(configuration);
		}
	}
}
