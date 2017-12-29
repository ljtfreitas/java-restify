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
package com.github.ljtfreitas.restify.http.client.jdk;

import java.net.Proxy;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Optional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public class HttpClientRequestConfiguration {

	private int connectionTimeout = 0;
	private int readTimeout = 0;
	private boolean followRedirects = true;
	private boolean useCaches = true;

	private Charset charset = Charset.forName("UTF-8");
	private Proxy proxy = null;

	private HttpClientRequestSsl ssl = new HttpClientRequestSsl();

	private HttpClientRequestConfiguration() {
	}

	private HttpClientRequestConfiguration(HttpClientRequestConfiguration source) {
		this.connectionTimeout = source.connectionTimeout;
		this.readTimeout = source.readTimeout;
		this.followRedirects = source.followRedirects;
		this.useCaches = source.useCaches;
		this.charset = source.charset;
		this.proxy = source.proxy;
		this.ssl = new HttpClientRequestSsl(source.ssl);
	}

	public int connectionTimeout() {
		return connectionTimeout;
	}

	public int readTimeout() {
		return readTimeout;
	}

	public boolean followRedirects() {
		return followRedirects;
	}

	public boolean useCaches() {
		return useCaches;
	}

	public Charset charset() {
		return charset;
	}

	public Optional<Proxy> proxy() {
		return Optional.ofNullable(proxy);
	}

	public HttpClientRequestSsl ssl() {
		return ssl;
	}

	public static HttpClientRequestConfiguration useDefault() {
		return new HttpClientRequestConfiguration();
	}

	public class HttpClientRequestSsl {

		private SSLSocketFactory sslSocketFactory;
		private HostnameVerifier hostnameVerifier;

		private HttpClientRequestSsl() {
		}
		
		private HttpClientRequestSsl(HttpClientRequestSsl source) {
			this.sslSocketFactory = source.sslSocketFactory;
			this.hostnameVerifier = source.hostnameVerifier;
		}

		public Optional<SSLSocketFactory> sslSocketFactory() {
			return Optional.ofNullable(sslSocketFactory);
		}

		public Optional<HostnameVerifier> hostnameVerifier() {
			return Optional.ofNullable(hostnameVerifier);
		}
	}

	public static class Builder {

		private HttpClientRequestConfiguration configuration = new HttpClientRequestConfiguration();

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

		public Builder charset(Charset charset) {
			configuration.charset = charset;
			return this;
		}

		public Builder proxy(Proxy proxy) {
			configuration.proxy = proxy;
			return this;
		}

		public FolllowRedirectsBuilder followRedirects() {
			return new FolllowRedirectsBuilder();
		}

		public Builder followRedirects(boolean enabled) {
			configuration.followRedirects = enabled;
			return this;
		}

		public UseCachesBuilder useCaches() {
			return new UseCachesBuilder();
		}

		public Builder useCaches(boolean enabled) {
			configuration.useCaches = enabled;
			return this;
		}

		public SslBuilder ssl() {
			return new SslBuilder();
		}

		public HttpClientRequestConfiguration build() {
			return new HttpClientRequestConfiguration(configuration);
		}

		public class FolllowRedirectsBuilder {

			public Builder enabled() {
				configuration.followRedirects = true;
				return Builder.this;
			}

			public Builder disabled() {
				configuration.followRedirects = false;
				return Builder.this;
			}
		}

		public class UseCachesBuilder {

			public Builder enabled() {
				configuration.useCaches = true;
				return Builder.this;
			}

			public Builder disabled() {
				configuration.followRedirects = false;
				return Builder.this;
			}
		}

		public class SslBuilder {

			public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
				configuration.ssl().sslSocketFactory = sslSocketFactory;
				return Builder.this;
			}

			public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
				configuration.ssl().hostnameVerifier = hostnameVerifier;
				return Builder.this;
			}

			public Builder and() {
				return Builder.this;
			}
		}
	}
}
