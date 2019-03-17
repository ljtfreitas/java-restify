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
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

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
import com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutorAdapter;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactoryAdapter;
import com.github.ljtfreitas.restify.http.client.request.async.DefaultAsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.async.interceptor.AsyncEndpointRequestInterceptorChain;
import com.github.ljtfreitas.restify.http.client.request.async.interceptor.AsyncInterceptedEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.authentication.Authentication;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.HttpClientRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.authentication.AuthenticationEndpoinRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.response.DefaultEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EmptyOnNotFoundEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.EndpointResponseRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.HeadersRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.StatusCodeRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.ThrowableRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryConfiguration;
import com.github.ljtfreitas.restify.http.client.retry.async.AsyncRetryableEndpointRequestExecutor;
import com.github.ljtfreitas.restify.util.Try;
import com.github.ljtfreitas.restify.util.async.DisposableExecutors;

public class HypermediaBrowserBuilder {

	private URL baseURL;

	private HttpClientRequestFactory httpClientRequestFactory;

	private EndpointRequestExecutor endpointRequestExecutor;

	private HttpMessageConvertersBuilder httpMessageConvertersBuilder = new HttpMessageConvertersBuilder(this);

	private EndpointRequestInterceptorsBuilder endpointRequestInterceptorsBuilder = new EndpointRequestInterceptorsBuilder(this);

	private EndpointResponseErrorFallbackBuilder endpointResponseErrorFallbackBuilder = new EndpointResponseErrorFallbackBuilder(this);

	private HttpClientRequestConfigurationBuilder httpClientRequestConfigurationBuilder = new HttpClientRequestConfigurationBuilder(this);

	private RetryBuilder retryBuilder = new RetryBuilder(this);

	private ResourceLinkDiscoveryBuilder resourceLinkDiscoveryBuilder = new ResourceLinkDiscoveryBuilder(this);

	private AsyncBuilder asyncBuilder = new AsyncBuilder(this);

	public HypermediaBrowserBuilder baseURL(String baseUrl) {
		this.baseURL = Try.of(() -> new URL(baseUrl)).get();
		return this;
	}

	public HypermediaBrowserBuilder baseURL(URL baseUrl) {
		this.baseURL = baseUrl;
		return this;
	}

	public HypermediaBrowserBuilder baseURL(URI baseUrl) {
		this.baseURL = Try.of(baseUrl::toURL).get();
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

	public AsyncBuilder async() {
		return asyncBuilder;
	}

	public RetryBuilder retry() {
		return retryBuilder;
	}

	public HypermediaBrowser build() {
		LinkRequestExecutor linkRequestExecutor = linkRequestExecutor();
		HypermediaLinkDiscovery resourceLinkDiscovery = resourceLinkDiscoveryBuilder.build();

		return new HypermediaBrowser(linkRequestExecutor, resourceLinkDiscovery, baseURL);
	}

	private LinkRequestExecutor linkRequestExecutor() {
		return new LinkRequestExecutor(retryable(intercepted(asyncEndpointRequestExecutor())));
	}

	private AsyncEndpointRequestExecutor retryable(AsyncEndpointRequestExecutor delegate) {
		RetryConfiguration configuration = retryBuilder.build();
		return configuration == null ?
				delegate :
					new AsyncRetryableEndpointRequestExecutor(delegate, retryBuilder.async.scheduler, configuration);
	}

	private AsyncEndpointRequestExecutor intercepted(AsyncEndpointRequestExecutor delegate) {
		Collection<EndpointRequestInterceptor> interceptors = endpointRequestInterceptorsBuilder.all;
		return interceptors.isEmpty() ?
				delegate :
					new AsyncInterceptedEndpointRequestExecutor(delegate, AsyncEndpointRequestInterceptorChain.of(interceptors));
	}

	private AsyncEndpointRequestExecutor asyncEndpointRequestExecutor() {
		EndpointRequestExecutor endpointRequestExecutor = Optional.ofNullable(this.endpointRequestExecutor)
				.orElseGet(this::defaultAsyncEndpointRequestExecutor);

		return endpointRequestExecutor instanceof AsyncEndpointRequestExecutor ?
				(AsyncEndpointRequestExecutor) endpointRequestExecutor :
					new AsyncEndpointRequestExecutorAdapter(asyncBuilder.pool, endpointRequestExecutor);
	}

	private AsyncEndpointRequestExecutor defaultAsyncEndpointRequestExecutor() {
		HttpMessageConverters messageConverters = httpMessageConvertersBuilder.build();
		EndpointRequestWriter writer = new EndpointRequestWriter(messageConverters);
		EndpointResponseReader reader = new EndpointResponseReader(messageConverters, endpointResponseErrorFallbackBuilder.build());

		return new DefaultAsyncEndpointRequestExecutor(asyncBuilder.pool, asyncHttpClientRequestFactory(), writer, reader);
	}

	private AsyncHttpClientRequestFactory asyncHttpClientRequestFactory() {
		HttpClientRequestFactory httpClientRequestFactory = Optional.ofNullable(this.httpClientRequestFactory)
				.orElseGet(() -> defaultHttpClientRequestFactory());

		return httpClientRequestFactory instanceof AsyncHttpClientRequestFactory ?
				(AsyncHttpClientRequestFactory) httpClientRequestFactory :
					new AsyncHttpClientRequestFactoryAdapter(asyncBuilder.pool, defaultHttpClientRequestFactory());
	}

	private JdkHttpClientRequestFactory defaultHttpClientRequestFactory() {
		return new JdkHttpClientRequestFactory(httpClientRequestConfiguration());
	}

	private HttpClientRequestConfiguration httpClientRequestConfiguration() {
		return httpClientRequestConfigurationBuilder.build();
	}

	public class EndpointRequestInterceptorsBuilder {

		private final HypermediaBrowserBuilder context;
		private final Collection<EndpointRequestInterceptor> all = new ArrayList<>();

		private EndpointRequestInterceptorsBuilder(HypermediaBrowserBuilder context) {
			this.context = context;
		}

		public EndpointRequestInterceptorsBuilder authentication(Authentication authentication) {
			all.add(new AuthenticationEndpoinRequestInterceptor(authentication));
			return this;
		}

		public EndpointRequestInterceptorsBuilder add(EndpointRequestInterceptor...interceptors) {
			this.all.addAll(Arrays.asList(interceptors));
			return this;
		}

		public HypermediaBrowserBuilder and() {
			return context;
		}

		public AsyncEndpointRequestInterceptorChain build() {
			return AsyncEndpointRequestInterceptorChain.of(all);
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
					.orElseGet(() -> emptyOnNotFound ? new EmptyOnNotFoundEndpointResponseErrorFallback()
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
		private final HttpClientRequestConfiguration.Builder httpClientRequestConfigurationBuilder = new HttpClientRequestConfiguration.Builder();
		private final HttpClientRequestInterceptorsBuilder interceptors = new HttpClientRequestInterceptorsBuilder();

		private HttpClientRequestConfiguration httpClientRequestConfiguration = null;

		private HttpClientRequestConfigurationBuilder(HypermediaBrowserBuilder context) {
			this.context = context;
		}

		public HttpClientRequestConfigurationBuilder connectionTimeout(int connectionTimeout) {
			httpClientRequestConfigurationBuilder.connectionTimeout(connectionTimeout);
			return this;
		}

		public HttpClientRequestConfigurationBuilder connectionTimeout(Duration connectionTimeout) {
			httpClientRequestConfigurationBuilder.connectionTimeout(connectionTimeout);
			return this;
		}

		public HttpClientRequestConfigurationBuilder readTimeout(int readTimeout) {
			httpClientRequestConfigurationBuilder.readTimeout(readTimeout);
			return this;
		}

		public HttpClientRequestConfigurationBuilder readTimeout(Duration readTimeout) {
			httpClientRequestConfigurationBuilder.readTimeout(readTimeout);
			return this;
		}

		public HttpClientRequestConfigurationBuilder charset(Charset charset) {
			httpClientRequestConfigurationBuilder.charset(charset);
			return this;
		}

		public HttpClientRequestConfigurationBuilder proxy(Proxy proxy) {
			httpClientRequestConfigurationBuilder.proxy(proxy);
			return this;
		}

		public HttpClientRequestFollowRedirectsConfigurationBuilder followRedirects() {
			return new HttpClientRequestFollowRedirectsConfigurationBuilder();
		}

		public HttpClientRequestConfigurationBuilder followRedirects(boolean enabled) {
			httpClientRequestConfigurationBuilder.followRedirects(enabled);
			return this;
		}

		public HttpClientRequestUseCachesConfigurationBuilder useCaches() {
			return new HttpClientRequestUseCachesConfigurationBuilder();
		}

		public HttpClientRequestConfigurationBuilder useCaches(boolean enabled) {
			httpClientRequestConfigurationBuilder.useCaches(enabled);
			return this;
		}

		public HttpClientRequestSslConfigurationBuilder ssl() {
			return new HttpClientRequestSslConfigurationBuilder();
		}

		public HttpClientRequestInterceptorsBuilder interceptors() {
			return interceptors;
		}

		public HypermediaBrowserBuilder interceptors(HttpClientRequestInterceptor...interceptors) {
			this.interceptors.add(interceptors);
			return context;
		}

		public HypermediaBrowserBuilder using(HttpClientRequestConfiguration httpClientRequestConfiguration) {
			this.httpClientRequestConfiguration = httpClientRequestConfiguration;
			return context;
		}

		public HypermediaBrowserBuilder and() {
			return context;
		}

		private HttpClientRequestConfiguration build() {
			return Optional.ofNullable(httpClientRequestConfiguration).orElseGet(() -> httpClientRequestConfigurationBuilder.build());
		}

		public class HttpClientRequestFollowRedirectsConfigurationBuilder {

			public HttpClientRequestConfigurationBuilder enabled() {
				httpClientRequestConfigurationBuilder.followRedirects().enabled();
				return HttpClientRequestConfigurationBuilder.this;
			}

			public HttpClientRequestConfigurationBuilder disabled() {
				httpClientRequestConfigurationBuilder.followRedirects().disabled();
				return HttpClientRequestConfigurationBuilder.this;
			}
		}

		public class HttpClientRequestUseCachesConfigurationBuilder {

			public HttpClientRequestConfigurationBuilder enabled() {
				httpClientRequestConfigurationBuilder.useCaches().enabled();
				return HttpClientRequestConfigurationBuilder.this;
			}

			public HttpClientRequestConfigurationBuilder disabled() {
				httpClientRequestConfigurationBuilder.useCaches().disabled();
				return HttpClientRequestConfigurationBuilder.this;
			}
		}

		public class HttpClientRequestSslConfigurationBuilder {

			public HttpClientRequestConfigurationBuilder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
				httpClientRequestConfigurationBuilder.ssl().sslSocketFactory(sslSocketFactory);
				return HttpClientRequestConfigurationBuilder.this;
			}

			public HttpClientRequestConfigurationBuilder hostnameVerifier(HostnameVerifier hostnameVerifier) {
				httpClientRequestConfigurationBuilder.ssl().hostnameVerifier(hostnameVerifier);
				return HttpClientRequestConfigurationBuilder.this;
			}

			public HypermediaBrowserBuilder and() {
				return context;
			}
		}

		public class HttpClientRequestInterceptorsBuilder {

			private final Collection<HttpClientRequestInterceptor> all = new ArrayList<>();

			public HttpClientRequestInterceptorsBuilder add(HttpClientRequestInterceptor... interceptors) {
				this.all.addAll(Arrays.asList(interceptors));
				return this;
			}

			public HypermediaBrowserBuilder and() {
				return context;
			}
		}
	}

	public class RetryBuilder {

		private final HypermediaBrowserBuilder context;
		private final RetryConfigurationBuilder builder = new RetryConfigurationBuilder();
		private final AsyncRetryConfigurationBuilder async = new AsyncRetryConfigurationBuilder(this);

		private boolean enabled = false;
		private RetryConfiguration configuration;

		public RetryBuilder(HypermediaBrowserBuilder context) {
			this.context = context;
		}

		public RetryBuilder enabled() {
			this.enabled = true;
			return this;
		}

		public HypermediaBrowserBuilder disabled() {
			this.enabled = false;
			return context;
		}

		public RetryConfigurationBuilder configure() {
			this.enabled = true;
			return builder;
		}

		public HypermediaBrowserBuilder using(RetryConfiguration configuration) {
			this.enabled = true;
			this.configuration = configuration;
			return context;
		}

		public AsyncRetryConfigurationBuilder async() {
			return async;
		}

		public HypermediaBrowserBuilder and() {
			return context;
		}

		private RetryConfiguration build() {
			return enabled ? Optional.ofNullable(configuration).orElseGet(() -> builder.build()) : null;
		}

		public class RetryConfigurationBuilder {

			private final RetryConfiguration.Builder delegate = new RetryConfiguration.Builder();
			private final RetryConfigurationBackOffBuilder backOff = new RetryConfigurationBackOffBuilder(this);

			public RetryConfigurationBuilder attempts(int attempts) {
				delegate.attempts(attempts);
				return this;
			}

			public RetryConfigurationBuilder timeout(long timeout) {
				delegate.timeout(timeout);
				return this;
			}

			public RetryConfigurationBuilder timeout(Duration timeout) {
				delegate.timeout(timeout);
				return this;
			}

			public RetryConfigurationBuilder when(HttpStatusCode... statuses) {
				delegate.when(statuses);
				return this;
			}

			public RetryConfigurationBuilder when(StatusCodeRetryCondition condition) {
				delegate.when(condition);
				return this;
			}

			@SafeVarargs
			public final RetryConfigurationBuilder when(Class<? extends Throwable>... throwableTypes) {
				delegate.when(throwableTypes);
				return this;
			}

			public final RetryConfigurationBuilder when(ThrowableRetryCondition condition) {
				delegate.when(condition);
				return this;
			}

			public final RetryConfigurationBuilder when(HeadersRetryCondition condition) {
				delegate.when(condition);
				return this;
			}

			public final RetryConfigurationBuilder when(EndpointResponseRetryCondition condition) {
				delegate.when(condition);
				return this;
			}

			public RetryConfigurationBackOffBuilder backOff() {
				return backOff;
			}

			public HypermediaBrowserBuilder and() {
				return context;
			}

			private RetryConfiguration build() {
				return delegate.build();
			}

			public class RetryConfigurationBackOffBuilder {

				private final RetryConfigurationBuilder context;

				private RetryConfigurationBackOffBuilder(RetryConfigurationBuilder context) {
					this.context = context;
				}

				public RetryConfigurationBackOffBuilder delay(long delay) {
					delegate.backOff().delay(delay);
					return this;
				}

				public RetryConfigurationBackOffBuilder delay(Duration delay) {
					delegate.backOff().delay(delay);
					return this;
				}

				public RetryConfigurationBackOffBuilder multiplier(double multiplier) {
					delegate.backOff().multiplier(multiplier);
					return this;
				}

				public RetryConfigurationBuilder and() {
					return context;
				}
			}
		}

		public class AsyncRetryConfigurationBuilder {

			private final RetryBuilder context;

			private ScheduledExecutorService scheduler = DisposableExecutors.newScheduledThreadPool(10);

			private AsyncRetryConfigurationBuilder(RetryBuilder context) {
				this.context = context;
			}

			public RetryBuilder scheduler(ScheduledExecutorService scheduler) {
				this.scheduler = scheduler;
				return context;
			}
		}
	}

	public class AsyncBuilder {

		private final HypermediaBrowserBuilder context;

		private Executor pool = DisposableExecutors.newCachedThreadPool();

		private AsyncBuilder(HypermediaBrowserBuilder context) {
			this.context = context;
		}

		public AsyncBuilder with(Executor executor) {
			this.pool = executor;
			return this;
		}

		public HypermediaBrowserBuilder and() {
			return context;
		}
	}
}
