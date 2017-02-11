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
package com.github.ljtfreitas.restify.http.client.request.jdk;

import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.client.charset.Encoding;

public class HttpClientRequestConfiguration {

	private int connectionTimeout = 0;
	private int readTimeout = 0;

	private Charset charset = Encoding.UTF_8.charset();

	private HttpClientRequestConfiguration() {
	}

	public int connectionTimeout() {
		return connectionTimeout;
	}

	public int readTimeout() {
		return readTimeout;
	}

	public Charset charset() {
		return charset;
	}

	public static HttpClientRequestConfiguration useDefault() {
		return new HttpClientRequestConfiguration();
	}

	public static class Builder {

		private HttpClientRequestConfiguration configuration = new HttpClientRequestConfiguration();

		public Builder connectionTimeout(int connectionTimeout) {
			configuration.connectionTimeout = connectionTimeout;
			return this;
		}

		public Builder readTimeout(int readTimeout) {
			configuration.readTimeout = readTimeout;
			return this;
		}

		public Builder charset(Charset charset) {
			configuration.charset = charset;
			return this;
		}

		public HttpClientRequestConfiguration build() {
			return configuration;
		}
	}
}
