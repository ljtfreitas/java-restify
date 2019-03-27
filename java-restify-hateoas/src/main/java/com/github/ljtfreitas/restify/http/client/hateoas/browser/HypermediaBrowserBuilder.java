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

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

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

	private final EndpointRequestExecutorBuilder endpointRequestExecutorBuilder = new EndpointRequestExecutorBuilder();

	private final HttpMessageConvertersBuilder httpMessageConvertersBuilder = new HttpMessageConvertersBuilder();

	private final EndpointResponseErrorFallbackBuilder endpointResponseErrorFallbackBuilder = new EndpointResponseErrorFallbackBuilder();

	private final HttpClientRequestFactoryBuilder httpClientRequestFactoryBuilder = new HttpClientRequestFactoryBuilder();

	private final RetryBuilder retryBuilder = new RetryBuilder();

	private final AsyncBuilder asyncBuilder = new AsyncBuilder();

	private final ResourceLinkDiscoveryBuilder resourceLinkDiscoveryBuilder = new ResourceLinkDiscoveryBuilder();

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

	public HypermediaBrowserBuilder executor(EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequestExecutorBuilder.using(endpointRequestExecutor);
		return this;
	}

	public EndpointRequestExecutorBuilder executor() {
		return endpointRequestExecutorBuilder;
	}

	public HypermediaBrowserBuilder converters(HttpMessageConverter...converters) {
		this.httpMessageConvertersBuilder.add(converters);
		return this;
	}

	public HttpMessageConvertersBuilder converters() {
		return this.httpMessageConvertersBuilder;
	}

	public HypermediaBrowserBuilder error(EndpointResponseErrorFallback fallback) {
		return endpointResponseErrorFallbackBuilder.using(fallback);
	}

	public EndpointResponseErrorFallbackBuilder error() {
		return endpointResponseErrorFallbackBuilder;
	}

	public RetryBuilder retry() {
		return retryBuilder;
	}

	public HypermediaBrowserBuilder async(Executor executor) {
		return this.asyncBuilder.using(executor);
	}

	public AsyncBuilder async() {
		return asyncBuilder;
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
		return new LinkRequestExecutor(retryable(intercepted(asyncEndpointRequestExecutor())));
	}

	private AsyncEndpointRequestExecutor retryable(AsyncEndpointRequestExecutor delegate) {
		RetryConfiguration configuration = retryBuilder.build();
		return configuration == null ?
				delegate :
					new AsyncRetryableEndpointRequestExecutor(delegate, retryBuilder.async.scheduler, configuration);
	}

	private AsyncEndpointRequestExecutor intercepted(AsyncEndpointRequestExecutor delegate) {
		Collection<EndpointRequestInterceptor> interceptors = endpointRequestExecutorBuilder.interceptors.all;
		return interceptors.isEmpty() ?
				delegate :
					new AsyncInterceptedEndpointRequestExecutor(delegate, AsyncEndpointRequestInterceptorChain.of(interceptors, asyncBuilder.executor));
	}

	private AsyncEndpointRequestExecutor asyncEndpointRequestExecutor() {
		EndpointRequestExecutor endpointRequestExecutor = Optional.ofNullable(endpointRequestExecutorBuilder.endpointRequestExecutor)
				.orElseGet(this::defaultAsyncEndpointRequestExecutor);

		return endpointRequestExecutor instanceof AsyncEndpointRequestExecutor ?
				(AsyncEndpointRequestExecutor) endpointRequestExecutor :
					new AsyncEndpointRequestExecutorAdapter(asyncBuilder.executor, endpointRequestExecutor);
	}

	private AsyncEndpointRequestExecutor defaultAsyncEndpointRequestExecutor() {
		HttpMessageConverters messageConverters = httpMessageConvertersBuilder.build();
		EndpointRequestWriter writer = new EndpointRequestWriter(messageConverters);
		EndpointResponseReader reader = new EndpointResponseReader(messageConverters, endpointResponseErrorFallbackBuilder.build());

		return new DefaultAsyncEndpointRequestExecutor(asyncBuilder.executor, asyncHttpClientRequestFactory(), writer, reader);
	}

	private AsyncHttpClientRequestFactory asyncHttpClientRequestFactory() {
		HttpClientRequestFactory httpClientRequestFactory = Optional.ofNullable(httpClientRequestFactoryBuilder.httpClientRequestFactory)
				.orElseGet(() -> defaultHttpClientRequestFactory());

		return httpClientRequestFactory instanceof AsyncHttpClientRequestFactory ?
				(AsyncHttpClientRequestFactory) httpClientRequestFactory :
					new AsyncHttpClientRequestFactoryAdapter(asyncBuilder.executor, defaultHttpClientRequestFactory());
	}

	private JdkHttpClientRequestFactory defaultHttpClientRequestFactory() {
		return new JdkHttpClientRequestFactory(httpClientRequestFactoryBuilder.configuration.build());
	}

	public class EndpointRequestExecutorBuilder {

		private final EndpointRequestInterceptorsBuilder interceptors = new EndpointRequestInterceptorsBuilder();

		private EndpointRequestExecutor endpointRequestExecutor = null;

		public EndpointRequestExecutorBuilder using(EndpointRequestExecutor endpointRequestExecutor) {
			this.endpointRequestExecutor = endpointRequestExecutor;
			return this;
		}

		public EndpointRequestInterceptorsBuilder interceptors() {
			return interceptors;
		}

		public EndpointRequestExecutorBuilder interceptors(EndpointRequestInterceptor...interceptors) {
			this.interceptors.add(interceptors);
			return this;
		}

		public HypermediaBrowserBuilder and() {
			return HypermediaBrowserBuilder.this;
		}

		public class EndpointRequestInterceptorsBuilder {

			private final Collection<EndpointRequestInterceptor> all = new ArrayList<>();

			public EndpointRequestInterceptorsBuilder authentication(Authentication authentication) {
				all.add(new AuthenticationEndpoinRequestInterceptor(authentication));
				return this;
			}

			public EndpointRequestInterceptorsBuilder add(EndpointRequestInterceptor...interceptors) {
				this.all.addAll(Arrays.asList(interceptors));
				return this;
			}

			public EndpointRequestExecutorBuilder and() {
				return EndpointRequestExecutorBuilder.this;
			}
		}
	}

	public class HttpMessageConvertersBuilder {

		private final Collection<HttpMessageConverter> converters = new ArrayList<>();

		private HttpMessageConvertersBuilder() {
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
			return HypermediaBrowserBuilder.this;
		}

		private HttpMessageConverters build() {
			return converters.size() == 1 ? all().build() : new HttpMessageConverters(converters);
		}
	}

	public class EndpointResponseErrorFallbackBuilder {

		private EndpointResponseErrorFallback fallback = null;
		private boolean emptyOnNotFound = false;

		private EndpointResponseErrorFallbackBuilder() {
			this(null);
		}

		private EndpointResponseErrorFallbackBuilder(EndpointResponseErrorFallback fallback) {
			this.fallback = fallback;
		}

		public HypermediaBrowserBuilder emptyOnNotFound() {
			this.emptyOnNotFound = true;
			return HypermediaBrowserBuilder.this;
		}

		public HypermediaBrowserBuilder using(EndpointResponseErrorFallback fallback) {
			this.fallback = fallback;
			return HypermediaBrowserBuilder.this;
		}

		private EndpointResponseErrorFallback build() {
			return Optional.ofNullable(fallback)
					.orElseGet(() -> emptyOnNotFound ? new EmptyOnNotFoundEndpointResponseErrorFallback() : new DefaultEndpointResponseErrorFallback());
		}
	}

	public class HttpClientRequestFactoryBuilder {

		private final HttpClientRequestConfigurationBuilder configuration = new HttpClientRequestConfigurationBuilder();
		private final HttpClientRequestInterceptorsBuilder interceptors = new HttpClientRequestInterceptorsBuilder();

		private HttpClientRequestFactory httpClientRequestFactory = null;

		public HttpClientRequestInterceptorsBuilder interceptors() {
			return interceptors;
		}

		public HttpClientRequestFactoryBuilder interceptors(HttpClientRequestInterceptor... interceptors) {
			this.interceptors.add(interceptors);
			return this;
		}

		public HttpClientRequestConfigurationBuilder configure() {
			return configuration;
		}

		public HttpClientRequestFactoryBuilder using(HttpClientRequestFactory httpClientRequestFactory) {
			this.httpClientRequestFactory = httpClientRequestFactory;
			return this;
		}

		public HypermediaBrowserBuilder and() {
			return HypermediaBrowserBuilder.this;
		}

		public class HttpClientRequestInterceptorsBuilder {

			private final Collection<HttpClientRequestInterceptor> all = new ArrayList<>();

			public HttpClientRequestInterceptorsBuilder add(HttpClientRequestInterceptor... interceptors) {
				this.all.addAll(Arrays.asList(interceptors));
				return this;
			}

			public HypermediaBrowserBuilder and() {
				return HypermediaBrowserBuilder.this;
			}
		}

		public class HttpClientRequestConfigurationBuilder {

			private final HttpClientRequestConfiguration.Builder delegate = new HttpClientRequestConfiguration.Builder();
			private final HttpClientRequestFollowRedirectsConfigurationBuilder followRedirects = new HttpClientRequestFollowRedirectsConfigurationBuilder();
			private final HttpClientRequestUseCachesConfigurationBuilder useCaches = new HttpClientRequestUseCachesConfigurationBuilder();
			private final HttpClientRequestBufferRequestBodyConfigurationBuilder bufferRequestBody = new HttpClientRequestBufferRequestBodyConfigurationBuilder();
			private final HttpClientRequestOutputStreamingConfigurationBuilder outputStreaming = new HttpClientRequestOutputStreamingConfigurationBuilder();
			private final HttpClientRequestSslConfigurationBuilder ssl = new HttpClientRequestSslConfigurationBuilder();

			private HttpClientRequestConfiguration httpClientRequestConfiguration = null;

			public HttpClientRequestConfigurationBuilder connectionTimeout(int connectionTimeout) {
				delegate.connectionTimeout(connectionTimeout);
				return this;
			}

			public HttpClientRequestConfigurationBuilder connectionTimeout(Duration connectionTimeout) {
				delegate.connectionTimeout(connectionTimeout);
				return this;
			}

			public HttpClientRequestConfigurationBuilder readTimeout(int readTimeout) {
				delegate.readTimeout(readTimeout);
				return this;
			}

			public HttpClientRequestConfigurationBuilder readTimeout(Duration readTimeout) {
				delegate.readTimeout(readTimeout);
				return this;
			}

			public HttpClientRequestConfigurationBuilder charset(Charset charset) {
				delegate.charset(charset);
				return this;
			}

			public HttpClientRequestConfigurationBuilder proxy(Proxy proxy) {
				delegate.proxy(proxy);
				return this;
			}

			public HttpClientRequestFollowRedirectsConfigurationBuilder followRedirects() {
				return followRedirects;
			}

			public HttpClientRequestConfigurationBuilder followRedirects(boolean enabled) {
				delegate.followRedirects(enabled);
				return this;
			}

			public HttpClientRequestUseCachesConfigurationBuilder useCaches() {
				return useCaches;
			}

			public HttpClientRequestConfigurationBuilder useCaches(boolean enabled) {
				delegate.useCaches(enabled);
				return this;
			}

			public HttpClientRequestBufferRequestBodyConfigurationBuilder bufferRequestBody() {
				return bufferRequestBody;
			}

			public HttpClientRequestConfigurationBuilder bufferRequestBody(boolean enabled) {
				delegate.bufferRequestBody(enabled);
				return this;
			}

			public HttpClientRequestOutputStreamingConfigurationBuilder outputStreaming() {
				return outputStreaming;
			}

			public HttpClientRequestConfigurationBuilder outputStreaming(boolean enabled) {
				delegate.outputStreaming(enabled);
				return this;
			}

			public HttpClientRequestSslConfigurationBuilder ssl() {
				return ssl;
			}

			public HttpClientRequestFactoryBuilder using(HttpClientRequestConfiguration httpClientRequestConfiguration) {
				this.httpClientRequestConfiguration = httpClientRequestConfiguration;
				return HttpClientRequestFactoryBuilder.this;
			}

			private HttpClientRequestConfiguration build() {
				return Optional.ofNullable(httpClientRequestConfiguration).orElseGet(() -> delegate.build());
			}

			public class HttpClientRequestFollowRedirectsConfigurationBuilder {

				public HttpClientRequestFactoryBuilder enabled() {
					delegate.followRedirects().enabled();
					return HttpClientRequestFactoryBuilder.this;
				}

				public HttpClientRequestFactoryBuilder disabled() {
					delegate.followRedirects().disabled();
					return HttpClientRequestFactoryBuilder.this;
				}
			}

			public class HttpClientRequestUseCachesConfigurationBuilder {

				public HttpClientRequestFactoryBuilder enabled() {
					delegate.useCaches().enabled();
					return HttpClientRequestFactoryBuilder.this;
				}

				public HttpClientRequestFactoryBuilder disabled() {
					delegate.useCaches().disabled();
					return HttpClientRequestFactoryBuilder.this;
				}
			}

			public class HttpClientRequestBufferRequestBodyConfigurationBuilder {

				public HttpClientRequestFactoryBuilder enabled() {
					delegate.bufferRequestBody().enabled();
					return HttpClientRequestFactoryBuilder.this;
				}

				public HttpClientRequestFactoryBuilder disabled() {
					delegate.bufferRequestBody().disabled();
					return HttpClientRequestFactoryBuilder.this;
				}
			}

			public class HttpClientRequestOutputStreamingConfigurationBuilder {

				public HttpClientRequestOutputStreamingConfigurationBuilder enabled() {
					delegate.outputStreaming().enabled();
					return this;
				}

				public HttpClientRequestFactoryBuilder disabled() {
					delegate.outputStreaming().disabled();
					return HttpClientRequestFactoryBuilder.this;
				}

				public HttpClientRequestOutputStreamingConfigurationBuilder chunkSize(int chunkSize) {
					delegate.outputStreaming().chunkSize(chunkSize);
					return this;
				}

				public HttpClientRequestFactoryBuilder and() {
					return HttpClientRequestFactoryBuilder.this;
				}
			}

			public class HttpClientRequestSslConfigurationBuilder {

				public HttpClientRequestSslConfigurationBuilder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
					delegate.ssl().sslSocketFactory(sslSocketFactory);
					return this;
				}

				public HttpClientRequestSslConfigurationBuilder hostnameVerifier(HostnameVerifier hostnameVerifier) {
					delegate.ssl().hostnameVerifier(hostnameVerifier);
					return this;
				}

				public HttpClientRequestFactoryBuilder and() {
					return HttpClientRequestFactoryBuilder.this;
				}
			}
		}
	}

	public class RetryBuilder {

		private final RetryConfigurationBuilder builder = new RetryConfigurationBuilder();
		private final AsyncRetryConfigurationBuilder async = new AsyncRetryConfigurationBuilder();

		private boolean enabled = false;
		private RetryConfiguration configuration;

		public RetryBuilder enabled() {
			this.enabled = true;
			return this;
		}

		public HypermediaBrowserBuilder disabled() {
			this.enabled = false;
			return HypermediaBrowserBuilder.this;
		}

		public RetryBuilder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		public RetryConfigurationBuilder configure() {
			this.enabled = true;
			return builder;
		}

		public HypermediaBrowserBuilder using(RetryConfiguration configuration) {
			this.enabled = true;
			this.configuration = configuration;
			return HypermediaBrowserBuilder.this;
		}

		public AsyncRetryConfigurationBuilder async() {
			return async;
		}

		public HypermediaBrowserBuilder and() {
			return HypermediaBrowserBuilder.this;
		}

		private RetryConfiguration build() {
			return enabled ? Optional.ofNullable(configuration).orElseGet(() -> builder.build()) : null;
		}

		public class RetryConfigurationBuilder {

			private final RetryConfiguration.Builder delegate = new RetryConfiguration.Builder();
			private final RetryConfigurationBackOffBuilder backOff = new RetryConfigurationBackOffBuilder();

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
				return HypermediaBrowserBuilder.this;
			}

			private RetryConfiguration build() {
				return delegate.build();
			}

			public class RetryConfigurationBackOffBuilder {

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
					return RetryConfigurationBuilder.this;
				}
			}
		}

		public class AsyncRetryConfigurationBuilder {

			private ScheduledExecutorService scheduler = DisposableExecutors.newScheduledThreadPool(10);

			public RetryBuilder scheduler(ScheduledExecutorService scheduler) {
				this.scheduler = nonNull(scheduler);
				return RetryBuilder.this;
			}
		}
	}

	public class ResourceLinkDiscoveryBuilder {

		private final Collection<LinkDiscovery> resolvers = new ArrayList<>();

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
			return HypermediaBrowserBuilder.this;
		}

		private HypermediaLinkDiscovery build() {
			return resolvers.isEmpty() ? all().build() : new HypermediaLinkDiscovery(resolvers);
		}
	}

	public class AsyncBuilder {

		private Executor executor = DisposableExecutors.newCachedThreadPool();;

		public HypermediaBrowserBuilder using(Executor executor) {
			this.executor = nonNull(executor);
			return HypermediaBrowserBuilder.this;
		}
	}
}
