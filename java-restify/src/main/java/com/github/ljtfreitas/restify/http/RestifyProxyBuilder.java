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
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import com.github.ljtfreitas.restify.http.client.call.DefaultEndpointCallFactory;
import com.github.ljtfreitas.restify.http.client.call.EndpointCallFactory;
import com.github.ljtfreitas.restify.http.client.call.EndpointMethodExecutor;
import com.github.ljtfreitas.restify.http.client.call.async.DefaultAsyncEndpointCallFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerProvider;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlers;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallObjectHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.HeadersEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.StatusCodeEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncCallbackEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallObjectHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.CallableEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.CollectionEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.CompletionStageCallbackEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.CompletionStageEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.EnumerationEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.FutureEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.FutureTaskEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.IterableEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.IteratorEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.ListIteratorEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.OptionalEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.QueueEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.RunnableEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.StreamEndpointCallHandlerAdapter;
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
import com.github.ljtfreitas.restify.http.client.request.async.interceptor.AsyncEndpointRequestInterceptorChain;
import com.github.ljtfreitas.restify.http.client.request.async.interceptor.AsyncHttpClientRequestInterceptorChain;
import com.github.ljtfreitas.restify.http.client.request.async.interceptor.AsyncInterceptedEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.async.interceptor.AsyncInterceptedHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.authentication.Authentication;
import com.github.ljtfreitas.restify.http.client.request.interceptor.AcceptVersionHeaderEndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptorChain;
import com.github.ljtfreitas.restify.http.client.request.interceptor.HeaderEndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.HttpClientRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.HttpClientRequestInterceptorChain;
import com.github.ljtfreitas.restify.http.client.request.interceptor.InterceptedEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.InterceptedHttpClientRequestFactory;
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
import com.github.ljtfreitas.restify.http.client.retry.RetryableEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.retry.async.AsyncRetryableEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.contract.metadata.Contract;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractExpressionResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractReader;
import com.github.ljtfreitas.restify.http.contract.metadata.DefaultContractReader;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointTarget;
import com.github.ljtfreitas.restify.http.contract.metadata.SimpleContractExpressionResolver;
import com.github.ljtfreitas.restify.spi.Provider;
import com.github.ljtfreitas.restify.util.async.DisposableExecutors;

public class RestifyProxyBuilder {

	private final ContractBuilder contractBuilder = new ContractBuilder();

	private final EndpointRequestExecutorBuilder endpointRequestExecutorBuilder = new EndpointRequestExecutorBuilder();

	private final HttpMessageConvertersBuilder httpMessageConvertersBuilder = new HttpMessageConvertersBuilder();

	private final EndpointCallHandlersBuilder endpointCallHandlersBuilder = new EndpointCallHandlersBuilder();

	private final EndpointResponseErrorFallbackBuilder endpointResponseErrorFallbackBuilder = new EndpointResponseErrorFallbackBuilder();

	private final HttpClientRequestFactoryBuilder httpClientRequestConfigurationBuilder = new HttpClientRequestFactoryBuilder();

	private final RetryBuilder retryBuilder = new RetryBuilder();

	private final Provider provider = new Provider();

	private ClassLoader classloader = null;

	private Executor asyncThreadPool = DisposableExecutors.newCachedThreadPool();

	public RestifyProxyBuilder client(HttpClientRequestFactory httpClientRequestFactory) {
		this.httpClientRequestConfigurationBuilder.httpClientRequestFactory = httpClientRequestFactory;
		return this;
	}

	public HttpClientRequestFactoryBuilder client() {
		return httpClientRequestConfigurationBuilder;
	}

	public RestifyProxyBuilder contract(ContractReader contract) {
		this.contractBuilder.contract = contract;
		return this;
	}

	public ContractBuilder contract() {
		return contractBuilder;
	}

	public RestifyProxyBuilder executor(EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequestExecutorBuilder.endpointRequestExecutor = endpointRequestExecutor;
		return this;
	}

	public EndpointRequestExecutorBuilder executor() {
		return endpointRequestExecutorBuilder;
	}

	public HttpMessageConvertersBuilder converters() {
		return this.httpMessageConvertersBuilder;
	}

	public RestifyProxyBuilder converters(HttpMessageConverter...converters) {
		this.httpMessageConvertersBuilder.add(converters);
		return this;
	}

	public EndpointCallHandlersBuilder handlers() {
		return this.endpointCallHandlersBuilder;
	}

	public RestifyProxyBuilder handlers(EndpointCallHandlerProvider providers) {
		this.endpointCallHandlersBuilder.add(providers);
		return this;
	}

	public EndpointResponseErrorFallbackBuilder error() {
		return endpointResponseErrorFallbackBuilder;
	}

	public RestifyProxyBuilder error(EndpointResponseErrorFallback fallback) {
		this.endpointResponseErrorFallbackBuilder.fallback = fallback;
		return this;
	}

	public RetryBuilder retry() {
		return retryBuilder;
	}

	public RestifyProxyBuilder classLoader(ClassLoader classLoader) {
		this.classloader = classLoader;
		return this;
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

			return new ProxyFactory(restifyProxyHandler, classloader).create(type);
		}

		private RestifyProxyHandler doBuild() {
			EndpointTarget target = new EndpointTarget(type, Optional.ofNullable(endpoint).orElse(null));

			EndpointMethodExecutor endpointMethodExecutor = new EndpointMethodExecutor(
					endpointRequestFactory(),
					endpointCallHandlers(),
					endpointCallFactory());

			Contract contract = contract();

			return new RestifyProxyHandler(contract.read(target), endpointMethodExecutor);
		}

		private EndpointRequestFactory endpointRequestFactory() {
			return new EndpointRequestFactory();
		}

		private EndpointCallHandlers endpointCallHandlers() {
			return endpointCallHandlersBuilder.build();
		}

		private EndpointCallFactory endpointCallFactory() {
			EndpointRequestExecutor executor = retryable(intercepted(endpointRequestExecutor()));
			return executor instanceof AsyncEndpointRequestExecutor ?
					asyncEndpointCallFactory(executor) :
						defaultEndpointCallFactory(executor);
		}

		private EndpointCallFactory asyncEndpointCallFactory(EndpointRequestExecutor executor) {
			EndpointCallFactory delegate = defaultEndpointCallFactory(executor);
			return new DefaultAsyncEndpointCallFactory((AsyncEndpointRequestExecutor) executor,
					endpointCallHandlersBuilder.async.executor,
					delegate);
		}

		private EndpointCallFactory defaultEndpointCallFactory(EndpointRequestExecutor executor) {
			return new DefaultEndpointCallFactory(executor);
		}

		private EndpointRequestExecutor endpointRequestExecutor() {
			return Optional.ofNullable(endpointRequestExecutorBuilder.endpointRequestExecutor)
				.orElseGet(() -> endpointRequestExecutor(intercepted(httpClientRequestFactory())));
		}

		private EndpointRequestExecutor intercepted(EndpointRequestExecutor delegate) {
			Collection<EndpointRequestInterceptor> interceptors = endpointRequestExecutorBuilder.interceptors.all;
			if (interceptors.isEmpty()) return delegate;
			else return delegate instanceof AsyncEndpointRequestExecutor ?
					new AsyncInterceptedEndpointRequestExecutor((AsyncEndpointRequestExecutor) delegate, AsyncEndpointRequestInterceptorChain.of(interceptors)) :
						new InterceptedEndpointRequestExecutor(delegate, new EndpointRequestInterceptorChain(interceptors));
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
			return new DefaultAsyncEndpointRequestExecutor(httpClientRequestConfigurationBuilder.async.executor,
					asyncHttpClientRequestFactory, writer, reader,
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
			return Optional.ofNullable(httpClientRequestConfigurationBuilder.httpClientRequestFactory)
					.orElseGet(() -> new JdkHttpClientRequestFactory(httpClientRequestConfiguration()));
		}

		private HttpClientRequestFactory intercepted(HttpClientRequestFactory delegate) {
			Collection<HttpClientRequestInterceptor> interceptors = httpClientRequestConfigurationBuilder.interceptors.all;
			if (interceptors.isEmpty()) return delegate;
			else return (delegate instanceof AsyncHttpClientRequestFactory) ?
					new AsyncInterceptedHttpClientRequestFactory((AsyncHttpClientRequestFactory) delegate, AsyncHttpClientRequestInterceptorChain.of(interceptors)) :
					new InterceptedHttpClientRequestFactory(delegate, new HttpClientRequestInterceptorChain(interceptors));
		}

		private HttpClientRequestConfiguration httpClientRequestConfiguration() {
			return httpClientRequestConfigurationBuilder.configuration.build();
		}

		private Contract contract() {
			return Optional.ofNullable(contractBuilder.contract)
					.map(c -> new Contract(c))
					.orElseGet(() -> new Contract(new DefaultContractReader(expressionResolver())));
		}

		private ContractExpressionResolver expressionResolver() {
			return Optional.ofNullable(contractBuilder.resolver)
					.orElseGet(() -> new SimpleContractExpressionResolver());
		}
	}

	public class ContractBuilder {

		private ContractReader contract = null;
		private ContractExpressionResolver resolver = null;

		public ContractBuilder using(ContractReader contract) {
			this.contract = contract;
			return this;
		}

		public ContractBuilder resolver(ContractExpressionResolver resolver) {
			this.resolver = resolver;
			return this;
		}

		public RestifyProxyBuilder and() {
			return RestifyProxyBuilder.this;
		}
	}
	
	public class HttpMessageConvertersBuilder {

		private final Collection<HttpMessageConverter> converters = new ArrayList<>();

		private final DiscoveryComponentConfigurationBuilder<HttpMessageConvertersBuilder> discoveryComponentConfiguration =
				new DiscoveryComponentConfigurationBuilder<>(this);

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
			return RestifyProxyBuilder.this;
		}

		private HttpMessageConverters build() {
			return converters.isEmpty() ? all().doBuild() : doBuild();
		}

		private HttpMessageConverters doBuild() {
			return new HttpMessageConverters(converters);
		}
	}

	public class EndpointCallHandlersBuilder {

		private final AsyncEndpointCallHandlersBuilder async = new AsyncEndpointCallHandlersBuilder();

		private final Collection<EndpointCallHandlerProvider> built = new ArrayList<>();
		private final Collection<EndpointCallHandlerProvider> providers = new ArrayList<>();

		private final DiscoveryComponentConfigurationBuilder<EndpointCallHandlersBuilder> discoveryComponentConfiguration =
				new DiscoveryComponentConfigurationBuilder<>(this);

		private EndpointCallHandlersBuilder() {
			this.built.add(OptionalEndpointCallHandlerFactory.instance());
			this.built.add(CallableEndpointCallHandlerFactory.instance());
			this.built.add(RunnableEndpointCallHandlerFactory.instance());
			this.built.add(CollectionEndpointCallHandlerFactory.instance());
			this.built.add(EnumerationEndpointCallHandlerAdapter.instance());
			this.built.add(IteratorEndpointCallHandlerAdapter.instance());
			this.built.add(ListIteratorEndpointCallHandlerAdapter.instance());
			this.built.add(IterableEndpointCallHandlerAdapter.instance());
			this.built.add(QueueEndpointCallHandlerAdapter.instance());
			this.built.add(StreamEndpointCallHandlerAdapter.instance());
			this.built.add(EndpointCallObjectHandlerAdapter.instance());
			this.built.add(HeadersEndpointCallHandlerAdapter.instance());
			this.built.add(StatusCodeEndpointCallHandlerAdapter.instance());
		}

		public EndpointCallHandlersBuilder async() {
			async.all();
			return this;
		}

		public EndpointCallHandlersBuilder async(Executor executor) {
			async.using(executor);
			return this;
		}

		public EndpointCallHandlersBuilder async(ExecutorService executorService) {
			async.using(executorService);
			return this;
		}

		public EndpointCallHandlersBuilder add(EndpointCallHandlerProvider provider) {
			providers.add(provider);
			return this;
		}

		public EndpointCallHandlersBuilder add(EndpointCallHandlerProvider...providers) {
			this.providers.addAll(Arrays.asList(providers));
			return this;
		}

		public DiscoveryComponentConfigurationBuilder<EndpointCallHandlersBuilder> discovery() {
			return discoveryComponentConfiguration;
		}

		public RestifyProxyBuilder and() {
			return RestifyProxyBuilder.this;
		}

		private EndpointCallHandlers build() {
			Collection<EndpointCallHandlerProvider> all = new ArrayList<>();

			all.addAll(providers);
			all.addAll(built);
			all.addAll(async.build());

			if (discoveryComponentConfiguration.enabled) all.addAll(provider.all(EndpointCallHandlerProvider.class));

			return new EndpointCallHandlers(all);
		}
	}

	private class AsyncEndpointCallHandlersBuilder {

		private final Collection<EndpointCallHandlerProvider> providers = new ArrayList<>();

		private Executor executor = asyncThreadPool;

		private AsyncEndpointCallHandlersBuilder all() {
			providers.add(new FutureEndpointCallHandlerAdapter<Object, Object>(executor));
			providers.add(new CompletionStageEndpointCallHandlerAdapter<Object, Object>(executor));
			providers.add(new CompletionStageCallbackEndpointCallHandlerAdapter<Object, Object>(executor));
			providers.add(new AsyncEndpointCallObjectHandlerAdapter<Object, Object>(executor));
			providers.add(new AsyncCallbackEndpointCallHandlerAdapter<Object, Object>(executor));

			if (executor instanceof ExecutorService) {
				providers.add(new FutureTaskEndpointCallHandlerAdapter<Object, Object>((ExecutorService) executor));
			}

			return this;
		}

		private AsyncEndpointCallHandlersBuilder using(Executor executor) {
			this.executor = executor;
			return this;
		}

		private Collection<EndpointCallHandlerProvider> build() {
			return providers.isEmpty() ? all().build() : providers;
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

		public RestifyProxyBuilder emptyOnNotFound() {
			this.emptyOnNotFound = true;
			return RestifyProxyBuilder.this;
		}

		public RestifyProxyBuilder using(EndpointResponseErrorFallback fallback) {
			this.fallback = fallback;
			return RestifyProxyBuilder.this;
		}

		private EndpointResponseErrorFallback build() {
			return Optional.ofNullable(fallback)
					.orElseGet(() -> emptyOnNotFound ? new EmptyOnNotFoundEndpointResponseErrorFallback() : new DefaultEndpointResponseErrorFallback());
		}
	}

	public class HttpClientRequestFactoryBuilder {

		private final HttpClientRequestConfigurationBuilder configuration = new HttpClientRequestConfigurationBuilder();
		private final HttpClientRequestInterceptorsBuilder interceptors = new HttpClientRequestInterceptorsBuilder();
		private final HttpClientRequestAsyncBuilder async = new HttpClientRequestAsyncBuilder();

		private HttpClientRequestFactory httpClientRequestFactory = null;

		public HttpClientRequestAsyncBuilder async() {
			return async;
		}

		public HttpClientRequestFactoryBuilder async(Executor executor) {
			async.using(executor);
			return this;
		}

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

		public RestifyProxyBuilder and() {
			return RestifyProxyBuilder.this;
		}

		private class HttpClientRequestAsyncBuilder {

			private Executor executor = asyncThreadPool;

			private RestifyProxyBuilder using(Executor executor) {
				this.executor = executor;
				return RestifyProxyBuilder.this;
			}
		}

		public class HttpClientRequestInterceptorsBuilder {

			private final Collection<HttpClientRequestInterceptor> all = new ArrayList<>();

			public HttpClientRequestInterceptorsBuilder add(HttpClientRequestInterceptor... interceptors) {
				this.all.addAll(Arrays.asList(interceptors));
				return this;
			}

			public RestifyProxyBuilder and() {
				return RestifyProxyBuilder.this;
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

		public RestifyProxyBuilder and() {
			return RestifyProxyBuilder.this;
		}

		public class EndpointRequestInterceptorsBuilder {

			private final Collection<EndpointRequestInterceptor> all = new ArrayList<>();

			public EndpointRequestInterceptorsBuilder authentication(Authentication authentication) {
				all.add(new AuthenticationEndpoinRequestInterceptor(authentication));
				return this;
			}

			public EndpointRequestInterceptorsBuilder acceptVersion() {
				all.add(new AcceptVersionHeaderEndpointRequestInterceptor());
				return this;
			}

			public EndpointRequestInterceptorsBuilder acceptVersion(String version) {
				all.add(new AcceptVersionHeaderEndpointRequestInterceptor(EndpointVersion.of(version)));
				return this;
			}

			public EndpointRequestInterceptorsBuilder acceptVersion(EndpointVersion version) {
				all.add(new AcceptVersionHeaderEndpointRequestInterceptor(version));
				return this;
			}

			public EndpointRequestInterceptorsBuilder headers(Header... headers) {
				this.all.add(new HeaderEndpointRequestInterceptor(headers));
				return this;
			}

			public EndpointRequestInterceptorsBuilder headers(Collection<Header> headers) {
				this.all.add(new HeaderEndpointRequestInterceptor(headers));
				return this;
			}

			public EndpointRequestInterceptorsBuilder headers(Headers headers) {
				this.all.add(new HeaderEndpointRequestInterceptor(headers));
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
	
	public class RetryBuilder {

		private final RetryConfigurationBuilder builder = new RetryConfigurationBuilder();
		private final AsyncRetryConfigurationBuilder async = new AsyncRetryConfigurationBuilder();

		private boolean enabled = false;
		private RetryConfiguration configuration;

		public RetryBuilder enabled() {
			this.enabled = true;
			return this;
		}

		public RestifyProxyBuilder disabled() {
			this.enabled = false;
			return RestifyProxyBuilder.this;
		}

		public RetryBuilder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		public RetryConfigurationBuilder configure() {
			this.enabled = true;
			return builder;
		}

		public RestifyProxyBuilder using(RetryConfiguration configuration) {
			this.enabled = true;
			this.configuration = configuration;
			return RestifyProxyBuilder.this;
		}

		public AsyncRetryConfigurationBuilder async() {
			return async;
		}

		public RestifyProxyBuilder and() {
			return RestifyProxyBuilder.this;
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

			public RestifyProxyBuilder and() {
				return RestifyProxyBuilder.this;
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

			private ScheduledExecutorService scheduler = DisposableExecutors.newSingleThreadScheduledExecutor();

			public RetryBuilder scheduler(ScheduledExecutorService scheduler) {
				this.scheduler = scheduler;
				return RetryBuilder.this;
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
