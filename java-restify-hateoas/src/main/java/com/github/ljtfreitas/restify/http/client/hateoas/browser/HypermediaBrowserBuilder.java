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
package com.github.ljtfreitas.restify.http.client.hateoas.browser;

import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import com.github.ljtfreitas.restify.http.client.hateoas.JacksonHypermediaJsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.discovery.HypermediaHalJsonPathLinkDiscovery;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.discovery.HypermediaJsonPathLinkDiscovery;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.discovery.HypermediaLinkDiscovery;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.discovery.JsonPathLinkDiscovery;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.discovery.LinkDiscovery;
import com.github.ljtfreitas.restify.http.client.hateoas.hal.JacksonHypermediaHalJsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.jdk.HttpClientRequestConfiguration;
import com.github.ljtfreitas.restify.http.client.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.wildcard.SimpleTextMessageConverter;
import com.github.ljtfreitas.restify.http.client.request.DefaultEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.authentication.Authentication;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;
import com.github.ljtfreitas.restify.http.client.request.interceptor.authentication.AuthenticationEndpoinRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.response.DefaultEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.util.Tryable;

public class HypermediaBrowserBuilder {

	private URL baseURL;

	private EndpointRequestExecutor endpointRequestExecutor;

	private HttpClientRequestFactory httpClientRequestFactory;

	private HttpClientRequestConfigurationBuilder httpClientRequestConfigurationBuilder = new HttpClientRequestConfigurationBuilder(this);

	private EndpointRequestInterceptorsBuilder endpointRequestInterceptorsBuilder = new EndpointRequestInterceptorsBuilder(this);

	private HttpMessageConvertersBuilder httpMessageConvertersBuilder = new HttpMessageConvertersBuilder(this);

	private EndpointResponseErrorFallbackBuilder endpointResponseErrorFallbackBuilder = new EndpointResponseErrorFallbackBuilder(this);

	private ResourceLinkDiscoveryBuilder resourceLinkDiscoveryBuilder = new ResourceLinkDiscoveryBuilder(this);

	public HypermediaBrowserBuilder baseURL(String baseUrl) {
		this.baseURL = Tryable.of(() -> new URL(baseUrl));
		return this;
	}

	public HypermediaBrowserBuilder baseURL(URL baseUrl) {
		this.baseURL = baseUrl;
		return this;
	}

	public HypermediaBrowserBuilder baseURL(URI baseUrl) {
		this.baseURL = Tryable.of(baseUrl::toURL);
		return this;
	}

	public HypermediaBrowserBuilder client(HttpClientRequestFactory httpClientRequestFactory) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		return this;
	}

	public HttpClientRequestConfigurationBuilder client() {
		return httpClientRequestConfigurationBuilder;
	}

	public HypermediaBrowserBuilder executor(EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequestExecutor = endpointRequestExecutor;
		return this;
	}

	public HttpMessageConvertersBuilder converters() {
		return this.httpMessageConvertersBuilder;
	}

	public HypermediaBrowserBuilder converters(HttpMessageConverter... converters) {
		this.httpMessageConvertersBuilder.add(converters);
		return this;
	}

	public EndpointRequestInterceptorsBuilder interceptors() {
		return this.endpointRequestInterceptorsBuilder;
	}

	public HypermediaBrowserBuilder interceptors(EndpointRequestInterceptor... interceptors) {
		this.endpointRequestInterceptorsBuilder.add(interceptors);
		return this;
	}

	public EndpointResponseErrorFallbackBuilder error() {
		return endpointResponseErrorFallbackBuilder;
	}

	public HypermediaBrowserBuilder error(EndpointResponseErrorFallback fallback) {
		this.endpointResponseErrorFallbackBuilder = new EndpointResponseErrorFallbackBuilder(this, fallback);
		return this;
	}

	public ResourceLinkDiscoveryBuilder discovery() {
		return resourceLinkDiscoveryBuilder;
	}

	public HypermediaBrowser build() {
		LinkRequestExecutor linkRequestExecutor = linkRequestExecutor();
		HypermediaLinkDiscovery resourceLinkDiscovery = resourceLinkDiscoveryBuilder.build();

		return new HypermediaBrowser(linkRequestExecutor, resourceLinkDiscovery, baseURL);
	}

	private LinkRequestExecutor linkRequestExecutor() {
		return new LinkRequestExecutor(endpointRequestExecutor(), endpointRequestInterceptorsBuilder.build());
	}

	private EndpointRequestExecutor endpointRequestExecutor() {
		HttpMessageConverters messageConverters = httpMessageConvertersBuilder.build();
		EndpointRequestWriter writer = new EndpointRequestWriter(messageConverters);
		EndpointResponseReader reader = new EndpointResponseReader(messageConverters, endpointResponseErrorFallbackBuilder.build());

		return Optional.ofNullable(endpointRequestExecutor)
				.orElseGet(() -> new DefaultEndpointRequestExecutor(httpClientRequestFactory(), writer, reader));
	}

	private HttpClientRequestFactory httpClientRequestFactory() {
		return Optional.ofNullable(httpClientRequestFactory)
				.orElseGet(() -> new JdkHttpClientRequestFactory(httpClientRequestConfiguration()));
	}

	private HttpClientRequestConfiguration httpClientRequestConfiguration() {
		return httpClientRequestConfigurationBuilder.build();
	}

	public class EndpointRequestInterceptorsBuilder {

		private final HypermediaBrowserBuilder context;
		private final Collection<EndpointRequestInterceptor> interceptors = new ArrayList<>();

		private EndpointRequestInterceptorsBuilder(HypermediaBrowserBuilder context) {
			this.context = context;
		}

		public EndpointRequestInterceptorsBuilder authentication(Authentication authentication) {
			interceptors.add(new AuthenticationEndpoinRequestInterceptor(authentication));
			return this;
		}

		public EndpointRequestInterceptorsBuilder add(EndpointRequestInterceptor... interceptors) {
			this.interceptors.addAll(Arrays.asList(interceptors));
			return this;
		}

		public HypermediaBrowserBuilder and() {
			return context;
		}

		private EndpointRequestInterceptorStack build() {
			return new EndpointRequestInterceptorStack(interceptors);
		}
	}

	public class EndpointResponseErrorFallbackBuilder {

		private final HypermediaBrowserBuilder context;

		private EndpointResponseErrorFallback fallback = null;
		private boolean emptyOnNotFound = false;

		private EndpointResponseErrorFallbackBuilder(HypermediaBrowserBuilder context) {
			this.context = context;
		}

		private EndpointResponseErrorFallbackBuilder(HypermediaBrowserBuilder context, EndpointResponseErrorFallback fallback) {
			this.context = context;
			this.fallback = fallback;
		}

		public HypermediaBrowserBuilder emptyOnNotFound() {
			this.emptyOnNotFound = true;
			return context;
		}

		public HypermediaBrowserBuilder using(EndpointResponseErrorFallback fallback) {
			this.fallback = fallback;
			return context;
		}

		private EndpointResponseErrorFallback build() {
			return Optional.ofNullable(fallback)
					.orElseGet(() -> emptyOnNotFound ? DefaultEndpointResponseErrorFallback.emptyOnNotFound()
							: new DefaultEndpointResponseErrorFallback());
		}
	}

	public class HttpMessageConvertersBuilder {

		private final HypermediaBrowserBuilder context;
		private final Collection<HttpMessageConverter> converters = new ArrayList<>();

		private HttpMessageConvertersBuilder(HypermediaBrowserBuilder context) {
			this.context = context;
			this.converters.add(new SimpleTextMessageConverter());
		}

		public HttpMessageConvertersBuilder json() {
			converters.add(JacksonHypermediaJsonMessageConverter.unfollow());
			return this;
		}

		public HttpMessageConvertersBuilder hal() {
			converters.add(JacksonHypermediaHalJsonMessageConverter.unfollow());
			return this;
		}

		public HttpMessageConvertersBuilder all() {
			return json().hal();
		}

		public HttpMessageConvertersBuilder add(HttpMessageConverter... converters) {
			this.converters.addAll(Arrays.asList(converters));
			return this;
		}

		public HypermediaBrowserBuilder and() {
			return context;
		}

		private HttpMessageConverters build() {
			return converters.size() == 1 ? all().build() : new HttpMessageConverters(converters);
		}
	}

	public class ResourceLinkDiscoveryBuilder {

		private final HypermediaBrowserBuilder context;
		private final Collection<LinkDiscovery> resolvers = new ArrayList<>();

		private ResourceLinkDiscoveryBuilder(HypermediaBrowserBuilder context) {
			this.context = context;
		}

		public ResourceLinkDiscoveryBuilder jsonLink() {
			resolvers.add(new HypermediaJsonPathLinkDiscovery());
			return this;
		}

		public ResourceLinkDiscoveryBuilder hal() {
			resolvers.add(new HypermediaHalJsonPathLinkDiscovery());
			return this;
		}

		public ResourceLinkDiscoveryBuilder jsonPath() {
			resolvers.add(new JsonPathLinkDiscovery("$.%s"));
			return this;
		}

		public ResourceLinkDiscoveryBuilder jsonPath(String template) {
			resolvers.add(new JsonPathLinkDiscovery(template));
			return this;
		}

		public ResourceLinkDiscoveryBuilder jsonPathTemplate(String jsonPathTemplate) {
			resolvers.add(new HypermediaHalJsonPathLinkDiscovery());
			return this;
		}

		public ResourceLinkDiscoveryBuilder all() {
			return jsonLink().hal();
		}

		public ResourceLinkDiscoveryBuilder add(LinkDiscovery... resolvers) {
			this.resolvers.addAll(Arrays.asList(resolvers));
			return this;
		}

		public HypermediaBrowserBuilder and() {
			return context;
		}

		private HypermediaLinkDiscovery build() {
			return resolvers.isEmpty() ? all().build() : new HypermediaLinkDiscovery(resolvers);
		}
	}

	public class HttpClientRequestConfigurationBuilder {

		private final HypermediaBrowserBuilder context;
		private final HttpClientRequestConfiguration.Builder builder = new HttpClientRequestConfiguration.Builder();

		private HttpClientRequestConfiguration httpClientRequestConfiguration = null;

		private HttpClientRequestConfigurationBuilder(HypermediaBrowserBuilder context) {
			this.context = context;
		}

		public HttpClientRequestConfigurationBuilder connectionTimeout(int connectionTimeout) {
			builder.connectionTimeout(connectionTimeout);
			return this;
		}

		public HttpClientRequestConfigurationBuilder connectionTimeout(Duration connectionTimeout) {
			builder.connectionTimeout(connectionTimeout);
			return this;
		}

		public HttpClientRequestConfigurationBuilder readTimeout(int readTimeout) {
			builder.readTimeout(readTimeout);
			return this;
		}

		public HttpClientRequestConfigurationBuilder readTimeout(Duration readTimeout) {
			builder.readTimeout(readTimeout);
			return this;
		}

		public HttpClientRequestConfigurationBuilder charset(Charset charset) {
			builder.charset(charset);
			return this;
		}

		public HttpClientRequestConfigurationBuilder proxy(Proxy proxy) {
			builder.proxy(proxy);
			return this;
		}

		public HttpClientRequestFollowRedirectsConfigurationBuilder followRedirects() {
			return new HttpClientRequestFollowRedirectsConfigurationBuilder();
		}

		public HttpClientRequestConfigurationBuilder followRedirects(boolean enabled) {
			builder.followRedirects(enabled);
			return this;
		}

		public HttpClientRequestUseCachesConfigurationBuilder useCaches() {
			return new HttpClientRequestUseCachesConfigurationBuilder();
		}

		public HttpClientRequestConfigurationBuilder useCaches(boolean enabled) {
			builder.useCaches(enabled);
			return this;
		}

		public HttpClientRequestSslConfigurationBuilder ssl() {
			return new HttpClientRequestSslConfigurationBuilder();
		}

		public HypermediaBrowserBuilder using(HttpClientRequestConfiguration httpClientRequestConfiguration) {
			this.httpClientRequestConfiguration = httpClientRequestConfiguration;
			return context;
		}

		public HypermediaBrowserBuilder and() {
			return context;
		}

		private HttpClientRequestConfiguration build() {
			return Optional.ofNullable(httpClientRequestConfiguration).orElseGet(() -> builder.build());
		}

		public class HttpClientRequestFollowRedirectsConfigurationBuilder {

			public HttpClientRequestConfigurationBuilder enabled() {
				builder.followRedirects().enabled();
				return HttpClientRequestConfigurationBuilder.this;
			}

			public HttpClientRequestConfigurationBuilder disabled() {
				builder.followRedirects().disabled();
				return HttpClientRequestConfigurationBuilder.this;
			}
		}

		public class HttpClientRequestUseCachesConfigurationBuilder {

			public HttpClientRequestConfigurationBuilder enabled() {
				builder.useCaches().enabled();
				return HttpClientRequestConfigurationBuilder.this;
			}

			public HttpClientRequestConfigurationBuilder disabled() {
				builder.useCaches().disabled();
				return HttpClientRequestConfigurationBuilder.this;
			}
		}

		public class HttpClientRequestSslConfigurationBuilder {

			public HttpClientRequestConfigurationBuilder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
				builder.ssl().sslSocketFactory(sslSocketFactory);
				return HttpClientRequestConfigurationBuilder.this;
			}

			public HttpClientRequestConfigurationBuilder hostnameVerifier(HostnameVerifier hostnameVerifier) {
				builder.ssl().hostnameVerifier(hostnameVerifier);
				return HttpClientRequestConfigurationBuilder.this;
			}

			public HypermediaBrowserBuilder and() {
				return context;
			}
		}
	}
}
