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
package com.github.ljtfreitas.restify.http;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import com.github.ljtfreitas.restify.http.client.call.DefaultEndpointCallFactory;
import com.github.ljtfreitas.restify.http.client.call.EndpointCallFactory;
import com.github.ljtfreitas.restify.http.client.call.EndpointMethodExecutor;
import com.github.ljtfreitas.restify.http.client.call.async.DefaultAsyncEndpointCallFactory;
import com.github.ljtfreitas.restify.http.client.call.async.ExecutorAsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.ExecutorAsyncEndpointCallFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutables;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallObjectExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.HeadersEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.StatusCodeEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncCallbackEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallObjectExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.CallableEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.CollectionEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.CompletableFutureCallbackEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.CompletableFutureEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.EnumerationEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.FutureEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.FutureTaskEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.IterableEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.IteratorEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.ListIteratorEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.OptionalEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.RunnableEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.jdk.HttpClientRequestConfiguration;
import com.github.ljtfreitas.restify.http.client.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.form.FormURLEncodedMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.multipart.MultipartFormMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.octet.OctetStreamMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.wildcard.WildcardMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.xml.XmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.request.DefaultEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.request.EndpointVersion;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.async.DefaultAsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.authentication.Authentication;
import com.github.ljtfreitas.restify.http.client.request.interceptor.AcceptVersionHeaderEndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;
import com.github.ljtfreitas.restify.http.client.request.interceptor.HeaderEndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.authentication.AuthenticationEndpoinRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.response.DefaultEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.EndpointResponseRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.HeadersRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.StatusCodeRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.ThrowableRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryConfiguration;
import com.github.ljtfreitas.restify.http.client.retry.RetryableEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.retry.async.AsyncRetryableEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.contract.metadata.Contract;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractExpressionResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractReader;
import com.github.ljtfreitas.restify.http.contract.metadata.DefaultContractReader;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointTarget;
import com.github.ljtfreitas.restify.http.contract.metadata.SimpleContractExpressionResolver;
import com.github.ljtfreitas.restify.spi.Provider;

public class RestifyProxyBuilder {

	private ContractReader contractReader;

	private ContractExpressionResolver expressionResolver;

	private HttpClientRequestFactory httpClientRequestFactory;

	private EndpointRequestExecutor endpointRequestExecutor;

	private HttpMessageConvertersBuilder httpMessageConvertersBuilder = new HttpMessageConvertersBuilder(this);

	private EndpointRequestInterceptorsBuilder endpointRequestInterceptorsBuilder = new EndpointRequestInterceptorsBuilder(this);

	private EndpointCallExecutablesBuilder endpointCallExecutablesBuilder = new EndpointCallExecutablesBuilder(this);

	private EndpointResponseErrorFallbackBuilder endpointResponseErrorFallbackBuilder = new EndpointResponseErrorFallbackBuilder(this);

	private HttpClientRequestConfigurationBuilder httpClientRequestConfigurationBuilder = new HttpClientRequestConfigurationBuilder(this);

	private RetryBuilder retryBuilder = new RetryBuilder(this);

	private Provider provider = new Provider();

	public RestifyProxyBuilder client(HttpClientRequestFactory httpClientRequestFactory) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		return this;
	}

	public HttpClientRequestConfigurationBuilder client() {
		return httpClientRequestConfigurationBuilder;
	}

	public RestifyProxyBuilder contract(ContractReader contract) {
		this.contractReader = contract;
		return this;
	}

	public RestifyProxyBuilder expression(ContractExpressionResolver expression) {
		this.expressionResolver = expression;
		return this;
	}

	public RestifyProxyBuilder executor(EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequestExecutor = endpointRequestExecutor;
		return this;
	}

	public HttpMessageConvertersBuilder converters() {
		return this.httpMessageConvertersBuilder;
	}

	public RestifyProxyBuilder converters(HttpMessageConverter...converters) {
		this.httpMessageConvertersBuilder.add(converters);
		return this;
	}

	public EndpointRequestInterceptorsBuilder interceptors() {
		return this.endpointRequestInterceptorsBuilder;
	}

	public RestifyProxyBuilder interceptors(EndpointRequestInterceptor...interceptors) {
		this.endpointRequestInterceptorsBuilder.add(interceptors);
		return this;
	}

	public EndpointCallExecutablesBuilder executables() {
		return this.endpointCallExecutablesBuilder;
	}

	public RestifyProxyBuilder executables(EndpointCallExecutableProvider providers) {
		this.endpointCallExecutablesBuilder.add(providers);
		return this;
	}

	public EndpointResponseErrorFallbackBuilder error() {
		return endpointResponseErrorFallbackBuilder;
	}

	public RestifyProxyBuilder error(EndpointResponseErrorFallback fallback) {
		this.endpointResponseErrorFallbackBuilder = new EndpointResponseErrorFallbackBuilder(this, fallback);
		return this;
	}

	public RetryBuilder retry() {
		return retryBuilder;
	}

	public <T> RestifyProxyBuilderOnTarget<T> target(Class<T> target) {
		return new RestifyProxyBuilderOnTarget<>(target, null);
	}

	public <T> RestifyProxyBuilderOnTarget<T> target(Class<T> target, String endpoint) {
		return new RestifyProxyBuilderOnTarget<>(target, endpoint);
	}

	public <T> RestifyProxyBuilderOnTarget<T> target(Class<T> target, URL endpoint) {
		return new RestifyProxyBuilderOnTarget<>(target, endpoint.toString());
	}

	public <T> RestifyProxyBuilderOnTarget<T> target(Class<T> target, URI endpoint) {
		return new RestifyProxyBuilderOnTarget<>(target, endpoint.toString());
	}

	public class RestifyProxyBuilderOnTarget<T> {
		private final Class<T> type;
		private final String endpoint;

		private RestifyProxyBuilderOnTarget(Class<T> type, String endpoint) {
			this.type = type;
			this.endpoint = endpoint;
		}

		public T build() {
			RestifyProxyHandler restifyProxyHandler = doBuild();

			return new ProxyFactory(restifyProxyHandler).create(type);
		}

		private RestifyProxyHandler doBuild() {
			EndpointTarget target = new EndpointTarget(type, Optional.ofNullable(endpoint).orElse(null));

			EndpointMethodExecutor endpointMethodExecutor = new EndpointMethodExecutor(
					endpointRequestFactory(),
					endpointCallExecutables(),
					endpointCallFactory());

			Contract contract = contract();

			return new RestifyProxyHandler(contract.read(target), endpointMethodExecutor);
		}

		private EndpointRequestFactory endpointRequestFactory() {
			return new EndpointRequestFactory(endpointRequestInterceptorsBuilder.build());
		}

		private EndpointCallExecutables endpointCallExecutables() {
			return endpointCallExecutablesBuilder.build();
		}

		private EndpointCallFactory endpointCallFactory() {
			EndpointRequestExecutor executor = retryable(endpointRequestExecutor());
			return executor instanceof AsyncEndpointRequestExecutor ?
					asyncEndpointCallFactory(executor) :
						executorEndpointCallFactory(executor);
		}

		private EndpointCallFactory asyncEndpointCallFactory(EndpointRequestExecutor executor) {
			EndpointCallFactory delegate = new DefaultEndpointCallFactory(executor);
			return new DefaultAsyncEndpointCallFactory((AsyncEndpointRequestExecutor) executor,
					endpointCallExecutablesBuilder.async.executor,
					delegate);
		}

		private EndpointCallFactory executorEndpointCallFactory(EndpointRequestExecutor executor) {
			EndpointCallFactory delegate = new DefaultEndpointCallFactory(executor);
			return new ExecutorAsyncEndpointCallFactory(delegate, endpointCallExecutablesBuilder.async.executor);
		}

		private EndpointRequestExecutor endpointRequestExecutor() {
			return Optional.ofNullable(endpointRequestExecutor)
				.orElseGet(() -> endpointRequestExecutor(httpClientRequestFactory()));
		}

		private EndpointRequestExecutor endpointRequestExecutor(HttpClientRequestFactory httpClientRequestFactory) {
			HttpMessageConverters httpMessageConverters = httpMessageConvertersBuilder.build();

			EndpointRequestWriter writer = new EndpointRequestWriter(httpMessageConverters);
			EndpointResponseReader reader = new EndpointResponseReader(httpMessageConverters, endpointResponseErrorFallbackBuilder());

			return httpClientRequestFactory instanceof AsyncHttpClientRequestFactory ?
					asyncEndpointRequestExecutor((AsyncHttpClientRequestFactory) httpClientRequestFactory, writer, reader) :
						endpointRequestExecutor(httpClientRequestFactory, writer, reader);
		}

		private EndpointRequestExecutor asyncEndpointRequestExecutor(AsyncHttpClientRequestFactory asyncHttpClientRequestFactory,
				EndpointRequestWriter writer, EndpointResponseReader reader) {
			return new DefaultAsyncEndpointRequestExecutor(endpointCallExecutablesBuilder.async.executor,
					(AsyncHttpClientRequestFactory) httpClientRequestFactory, writer, reader,
						endpointRequestExecutor(asyncHttpClientRequestFactory, writer, reader));
		}

		private EndpointRequestExecutor endpointRequestExecutor(HttpClientRequestFactory httpClientRequestFactory,
				EndpointRequestWriter writer, EndpointResponseReader reader) {
			return new DefaultEndpointRequestExecutor(httpClientRequestFactory, writer, reader);
		}

		private EndpointRequestExecutor retryable(EndpointRequestExecutor delegate) {
			RetryConfiguration configuration = retryBuilder.build();
			return configuration == null ? delegate :
				delegate instanceof AsyncEndpointRequestExecutor ?
						new AsyncRetryableEndpointRequestExecutor((AsyncEndpointRequestExecutor) delegate, retryBuilder.async.scheduler, configuration) :
							new RetryableEndpointRequestExecutor(delegate, configuration);
		}

		private EndpointResponseErrorFallback endpointResponseErrorFallbackBuilder() {
			return endpointResponseErrorFallbackBuilder.build();
		}

		private HttpClientRequestFactory httpClientRequestFactory() {
			return Optional.ofNullable(httpClientRequestFactory)
					.orElseGet(() -> new JdkHttpClientRequestFactory(httpClientRequestConfiguration()));
		}

		private HttpClientRequestConfiguration httpClientRequestConfiguration() {
			return httpClientRequestConfigurationBuilder.build();
		}

		private Contract contract() {
			return Optional.ofNullable(contractReader)
					.map(c -> new Contract(c))
					.orElseGet(() -> new Contract(new DefaultContractReader(expressionResolver())));
		}

		private ContractExpressionResolver expressionResolver() {
			return Optional.ofNullable(expressionResolver)
					.orElseGet(() -> new SimpleContractExpressionResolver());
		}
	}

	public class HttpMessageConvertersBuilder {

		private final RestifyProxyBuilder context;
		private final Collection<HttpMessageConverter> converters = new ArrayList<>();

		private final DiscoveryComponentConfigurationBuilder<HttpMessageConvertersBuilder> discoveryComponentConfiguration =
				new DiscoveryComponentConfigurationBuilder<>(this);
		
		private HttpMessageConvertersBuilder(RestifyProxyBuilder context) {
			this.context = context;
		}

		public HttpMessageConvertersBuilder wildcard() {
			converters.addAll(provider.all(WildcardMessageConverter.class));
			return this;
		}

		public HttpMessageConvertersBuilder octetStream() {
			converters.addAll(provider.all(OctetStreamMessageConverter.class));
			return this;
		}

		public HttpMessageConvertersBuilder json() {
			provider.single(JsonMessageConverter.class).ifPresent(converters::add);
			return this;
		}

		public HttpMessageConvertersBuilder xml() {
			provider.single(XmlMessageConverter.class).ifPresent(converters::add);
			return this;
		}

		public HttpMessageConvertersBuilder text() {
			converters.addAll(provider.all(TextMessageConverter.class));
			return this;
		}

		public HttpMessageConvertersBuilder form() {
			converters.addAll(provider.all(FormURLEncodedMessageConverter.class));
			converters.addAll(provider.all(MultipartFormMessageWriter.class));
			return this;
		}

		public HttpMessageConvertersBuilder all() {
			return discoveryComponentConfiguration.enabled ? wildcard().json().xml().text().form().octetStream().auto() : this;
		}
		
		private HttpMessageConvertersBuilder auto() {
			converters.addAll(provider.all(HttpMessageConverter.class));
			return this;
		} 

		public HttpMessageConvertersBuilder add(HttpMessageConverter...converters) {
			this.converters.addAll(Arrays.asList(converters));
			return this;
		}

		public DiscoveryComponentConfigurationBuilder<HttpMessageConvertersBuilder> discovery() {
			return discoveryComponentConfiguration;
		}
		
		public RestifyProxyBuilder and() {
			return context;
		}

		private HttpMessageConverters build() {
			return converters.isEmpty() ? all().doBuild() : doBuild();
		}

		private HttpMessageConverters doBuild() {
			return new HttpMessageConverters(converters);
		}
	}

	public class EndpointRequestInterceptorsBuilder {

		private final RestifyProxyBuilder context;
		private final Collection<EndpointRequestInterceptor> interceptors = new ArrayList<>();

		private EndpointRequestInterceptorsBuilder(RestifyProxyBuilder context) {
			this.context = context;
		}

		public EndpointRequestInterceptorsBuilder authentication(Authentication authentication) {
			interceptors.add(new AuthenticationEndpoinRequestInterceptor(authentication));
			return this;
		}

		public EndpointRequestInterceptorsBuilder acceptVersion() {
			interceptors.add(new AcceptVersionHeaderEndpointRequestInterceptor());
			return this;
		}

		public EndpointRequestInterceptorsBuilder acceptVersion(String version) {
			interceptors.add(new AcceptVersionHeaderEndpointRequestInterceptor(EndpointVersion.of(version)));
			return this;
		}

		public EndpointRequestInterceptorsBuilder acceptVersion(EndpointVersion version) {
			interceptors.add(new AcceptVersionHeaderEndpointRequestInterceptor(version));
			return this;
		}

		public EndpointRequestInterceptorsBuilder headers(Header... headers) {
			this.interceptors.add(new HeaderEndpointRequestInterceptor(headers));
			return this;
		}

		public EndpointRequestInterceptorsBuilder headers(Collection<Header> headers) {
			this.interceptors.add(new HeaderEndpointRequestInterceptor(headers));
			return this;
		}

		public EndpointRequestInterceptorsBuilder headers(Headers headers) {
			this.interceptors.add(new HeaderEndpointRequestInterceptor(headers));
			return this;
		}

		public EndpointRequestInterceptorsBuilder add(EndpointRequestInterceptor...interceptors) {
			this.interceptors.addAll(Arrays.asList(interceptors));
			return this;
		}

		public RestifyProxyBuilder and() {
			return context;
		}

		private EndpointRequestInterceptorStack build() {
			return new EndpointRequestInterceptorStack(interceptors);
		}
	}

	public class EndpointCallExecutablesBuilder {

		private final RestifyProxyBuilder context;
		private final AsyncEndpointCallExecutablesBuilder async = new AsyncEndpointCallExecutablesBuilder();

		private final Collection<EndpointCallExecutableProvider> built = new ArrayList<>();
		private final Collection<EndpointCallExecutableProvider> providers = new ArrayList<>();

		private final DiscoveryComponentConfigurationBuilder<EndpointCallExecutablesBuilder> discoveryComponentConfiguration =
				new DiscoveryComponentConfigurationBuilder<>(this);

		private EndpointCallExecutablesBuilder(RestifyProxyBuilder context) {
			this.context = context;
			this.built.add(OptionalEndpointCallExecutableFactory.instance());
			this.built.add(CallableEndpointCallExecutableFactory.instance());
			this.built.add(RunnableEndpointCallExecutableFactory.instance());
			this.built.add(CollectionEndpointCallExecutableFactory.instance());
			this.built.add(EnumerationEndpointCallExecutableFactory.instance());
			this.built.add(IteratorEndpointCallExecutableFactory.instance());
			this.built.add(ListIteratorEndpointCallExecutableFactory.instance());
			this.built.add(IterableEndpointCallExecutableFactory.instance());
			this.built.add(EndpointCallObjectExecutableFactory.instance());
			this.built.add(HeadersEndpointCallExecutableFactory.instance());
			this.built.add(StatusCodeEndpointCallExecutableFactory.instance());
		}

		public EndpointCallExecutablesBuilder async() {
			async.all();
			return this;
		}

		public EndpointCallExecutablesBuilder async(Executor executor) {
			async.with(executor);
			return this;
		}

		public EndpointCallExecutablesBuilder async(ExecutorService executorService) {
			async.with(executorService);
			return this;
		}

		public EndpointCallExecutablesBuilder add(EndpointCallExecutableProvider endpointCallExecutableProvider) {
			providers.add(endpointCallExecutableProvider);
			return this;
		}

		public EndpointCallExecutablesBuilder add(EndpointCallExecutableProvider...providers) {
			this.providers.addAll(Arrays.asList(providers));
			return this;
		}

		public DiscoveryComponentConfigurationBuilder<EndpointCallExecutablesBuilder> discovery() {
			return discoveryComponentConfiguration;
		}

		public RestifyProxyBuilder and() {
			return context;
		}

		private EndpointCallExecutables build() {
			Collection<EndpointCallExecutableProvider> all = new ArrayList<>();

			all.addAll(providers);
			all.addAll(built);
			all.addAll(async.build());

			if (discoveryComponentConfiguration.enabled) all.addAll(provider.all(EndpointCallExecutableProvider.class));

			return new EndpointCallExecutables(all);
		}
	}

	private class AsyncEndpointCallExecutablesBuilder {

		private final Collection<EndpointCallExecutableProvider> providers = new ArrayList<>();

		private Executor executor = ExecutorAsyncEndpointCall.pool();

		private AsyncEndpointCallExecutablesBuilder all() {
			providers.add(new FutureEndpointCallExecutableFactory<Object, Object>(executor));
			providers.add(new CompletableFutureEndpointCallExecutableFactory<Object, Object>(executor));
			providers.add(new CompletableFutureCallbackEndpointCallExecutableFactory<Object, Object>(executor));
			providers.add(new AsyncEndpointCallObjectExecutableFactory<Object, Object>(executor));
			providers.add(new AsyncCallbackEndpointCallExecutableFactory<Object, Object>(executor));

			if (executor instanceof ExecutorService) {
				providers.add(new FutureTaskEndpointCallExecutableFactory<Object, Object>((ExecutorService) executor));
			}

			return this;
		}

		private AsyncEndpointCallExecutablesBuilder with(Executor executor) {
			this.executor = executor;
			return this;
		}

		private Collection<EndpointCallExecutableProvider> build() {
			return providers.isEmpty() ? all().build() : providers;
		}
	}

	public class EndpointResponseErrorFallbackBuilder {

		private final RestifyProxyBuilder context;

		private EndpointResponseErrorFallback fallback = null;
		private boolean emptyOnNotFound = false;

		private EndpointResponseErrorFallbackBuilder(RestifyProxyBuilder context) {
			this.context = context;
		}

		private EndpointResponseErrorFallbackBuilder(RestifyProxyBuilder context, EndpointResponseErrorFallback fallback) {
			this.context = context;
			this.fallback = fallback;
		}

		public RestifyProxyBuilder emptyOnNotFound() {
			this.emptyOnNotFound = true;
			return context;
		}

		public RestifyProxyBuilder using(EndpointResponseErrorFallback fallback) {
			this.fallback = fallback;
			return context;
		}

		private EndpointResponseErrorFallback build() {
			return Optional.ofNullable(fallback)
					.orElseGet(() -> emptyOnNotFound ? DefaultEndpointResponseErrorFallback.emptyOnNotFound() : new DefaultEndpointResponseErrorFallback());
		}
	}

	public class HttpClientRequestConfigurationBuilder {

		private final RestifyProxyBuilder context;
		private final HttpClientRequestConfiguration.Builder builder = new HttpClientRequestConfiguration.Builder();

		private HttpClientRequestConfiguration httpClientRequestConfiguration = null;

		private HttpClientRequestConfigurationBuilder(RestifyProxyBuilder context) {
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

		public RestifyProxyBuilder using(HttpClientRequestConfiguration httpClientRequestConfiguration) {
			this.httpClientRequestConfiguration = httpClientRequestConfiguration;
			return context;
		}

		public RestifyProxyBuilder and() {
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

			public RestifyProxyBuilder and() {
				return context;
			}
		}
	}

	public class RetryBuilder {

		private final RestifyProxyBuilder context;
		private final RetryConfigurationBuilder builder = new RetryConfigurationBuilder();
		private final AsyncRetryConfigurationBuilder async = new AsyncRetryConfigurationBuilder(this);

		private boolean enabled = false;
		private RetryConfiguration configuration;

		public RetryBuilder(RestifyProxyBuilder context) {
			this.context = context;
		}

		public RetryBuilder enabled() {
			this.enabled = true;
			return this;
		}

		public RetryConfigurationBuilder configure() {
			this.enabled = true;
			return builder;
		}

		public RestifyProxyBuilder using(RetryConfiguration configuration) {
			this.enabled = true;
			this.configuration = configuration;
			return context;
		}

		public AsyncRetryConfigurationBuilder async() {
			return async;
		}

		public RestifyProxyBuilder and() {
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

			public RestifyProxyBuilder and() {
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
					builder.backOff().delay(delay);
					return this;
				}

				public RetryConfigurationBackOffBuilder delay(Duration delay) {
					builder.backOff().delay(delay);
					return this;
				}

				public RetryConfigurationBackOffBuilder multiplier(double multiplier) {
					builder.backOff().multiplier(multiplier);
					return this;
				}

				public RetryConfigurationBuilder and() {
					return context;
				}
			}
		}

		public class AsyncRetryConfigurationBuilder {

			private final RetryBuilder context;

			private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

			private AsyncRetryConfigurationBuilder(RetryBuilder context) {
				this.context = context;
			}

			public RetryBuilder scheduler(ScheduledExecutorService scheduler) {
				this.scheduler = scheduler;
				return context;
			}
		}
	}

	public class DiscoveryComponentConfigurationBuilder<C> {

		private final C context;
		private boolean enabled = true;

		private DiscoveryComponentConfigurationBuilder(C context) {
			this.context = context;
		}

		public C enabled() {
			this.enabled = true;
			return context;
		}

		public C disabled() {
			this.enabled = false;
			return context;
		}
	}
}
