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
<<<<<<< HEAD
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutables;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallObjectExecutableAdapter;
import com.github.ljtfreitas.restify.http.client.call.exec.HeadersEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.client.call.exec.StatusCodeEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncCallbackEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallObjectExecutableAdapter;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.CallableEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.CollectionEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.CompletionStageCallbackEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.CompletionStageEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.EnumerationEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.FutureEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.FutureTaskEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.IterableEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.IteratorEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.ListIteratorEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.OptionalEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.RunnableEndpointCallExecutableFactory;
=======
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
import com.github.ljtfreitas.restify.http.client.call.handler.jdk.RunnableEndpointCallHandlerFactory;
>>>>>>> ea4d3f4... Mudança de nomes
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

<<<<<<< HEAD
	private EndpointCallExecutablesBuilder endpointCallExecutablesBuilder = new EndpointCallExecutablesBuilder(this);
=======
	private EndpointCallHandlersBuilder endpointCallHandlersBuilder = new EndpointCallHandlersBuilder(this);
>>>>>>> ea4d3f4... Mudança de nomes

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

<<<<<<< HEAD
	public EndpointCallExecutablesBuilder executables() {
		return this.endpointCallExecutablesBuilder;
	}

	public RestifyProxyBuilder executables(EndpointCallExecutableProvider providers) {
		this.endpointCallExecutablesBuilder.add(providers);
=======
	public EndpointCallHandlersBuilder handlers() {
		return this.endpointCallHandlersBuilder;
	}

	public RestifyProxyBuilder handlers(EndpointCallHandlerProvider providers) {
		this.endpointCallHandlersBuilder.add(providers);
>>>>>>> ea4d3f4... Mudança de nomes
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
			return new EndpointRequestFactory();
		}

<<<<<<< HEAD
		private EndpointCallExecutables endpointCallExecutables() {
			return endpointCallExecutablesBuilder.build();
=======
		private EndpointCallHandlers endpointCallExecutables() {
			return endpointCallHandlersBuilder.build();
>>>>>>> ea4d3f4... Mudança de nomes
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
<<<<<<< HEAD
					endpointCallExecutablesBuilder.async.executor,
=======
					endpointCallHandlersBuilder.async.executor,
>>>>>>> ea4d3f4... Mudança de nomes
					delegate);
		}

		private EndpointCallFactory defaultEndpointCallFactory(EndpointRequestExecutor executor) {
			return new DefaultEndpointCallFactory(executor);
		}

		private EndpointRequestExecutor endpointRequestExecutor() {
			return Optional.ofNullable(endpointRequestExecutor)
				.orElseGet(() -> endpointRequestExecutor(intercepted(httpClientRequestFactory())));
		}

		private EndpointRequestExecutor intercepted(EndpointRequestExecutor delegate) {
			Collection<EndpointRequestInterceptor> interceptors = endpointRequestInterceptorsBuilder.all;
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
<<<<<<< HEAD
			return new DefaultAsyncEndpointRequestExecutor(endpointCallExecutablesBuilder.async.executor,
=======
			return new DefaultAsyncEndpointRequestExecutor(endpointCallHandlersBuilder.async.executor,
>>>>>>> ea4d3f4... Mudança de nomes
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

		private HttpClientRequestFactory intercepted(HttpClientRequestFactory delegate) {
			Collection<HttpClientRequestInterceptor> interceptors = httpClientRequestConfigurationBuilder.interceptors.all;
			if (interceptors.isEmpty()) return delegate;
			else return (delegate instanceof AsyncHttpClientRequestFactory) ?
					new AsyncInterceptedHttpClientRequestFactory((AsyncHttpClientRequestFactory) delegate, AsyncHttpClientRequestInterceptorChain.of(interceptors)) :
					new InterceptedHttpClientRequestFactory(delegate, new HttpClientRequestInterceptorChain(interceptors));
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
		private final Collection<EndpointRequestInterceptor> all = new ArrayList<>();

		private EndpointRequestInterceptorsBuilder(RestifyProxyBuilder context) {
			this.context = context;
		}

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

		public RestifyProxyBuilder and() {
			return context;
		}
	}

<<<<<<< HEAD
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
			this.built.add(EnumerationEndpointCallExecutableAdapter.instance());
			this.built.add(IteratorEndpointCallExecutableAdapter.instance());
			this.built.add(ListIteratorEndpointCallExecutableAdapter.instance());
			this.built.add(IterableEndpointCallExecutableAdapter.instance());
			this.built.add(EndpointCallObjectExecutableAdapter.instance());
			this.built.add(HeadersEndpointCallExecutableAdapter.instance());
			this.built.add(StatusCodeEndpointCallExecutableAdapter.instance());
		}

		public EndpointCallExecutablesBuilder async() {
=======
	public class EndpointCallHandlersBuilder {

		private final RestifyProxyBuilder context;
		private final AsyncEndpointCallHandlerBuilder async = new AsyncEndpointCallHandlerBuilder();

		private final Collection<EndpointCallHandlerProvider> built = new ArrayList<>();
		private final Collection<EndpointCallHandlerProvider> providers = new ArrayList<>();

		private final DiscoveryComponentConfigurationBuilder<EndpointCallHandlersBuilder> discoveryComponentConfiguration =
				new DiscoveryComponentConfigurationBuilder<>(this);

		private EndpointCallHandlersBuilder(RestifyProxyBuilder context) {
			this.context = context;
			this.built.add(OptionalEndpointCallHandlerFactory.instance());
			this.built.add(CallableEndpointCallHandlerFactory.instance());
			this.built.add(RunnableEndpointCallHandlerFactory.instance());
			this.built.add(CollectionEndpointCallHandlerFactory.instance());
			this.built.add(EnumerationEndpointCallHandlerAdapter.instance());
			this.built.add(IteratorEndpointCallHandlerAdapter.instance());
			this.built.add(ListIteratorEndpointCallHandlerAdapter.instance());
			this.built.add(IterableEndpointCallHandlerAdapter.instance());
			this.built.add(EndpointCallObjectHandlerAdapter.instance());
			this.built.add(HeadersEndpointCallHandlerAdapter.instance());
			this.built.add(StatusCodeEndpointCallHandlerAdapter.instance());
		}

		public EndpointCallHandlersBuilder async() {
>>>>>>> ea4d3f4... Mudança de nomes
			async.all();
			return this;
		}

<<<<<<< HEAD
		public EndpointCallExecutablesBuilder async(Executor executor) {
=======
		public EndpointCallHandlersBuilder async(Executor executor) {
>>>>>>> ea4d3f4... Mudança de nomes
			async.with(executor);
			return this;
		}

<<<<<<< HEAD
		public EndpointCallExecutablesBuilder async(ExecutorService executorService) {
=======
		public EndpointCallHandlersBuilder async(ExecutorService executorService) {
>>>>>>> ea4d3f4... Mudança de nomes
			async.with(executorService);
			return this;
		}

<<<<<<< HEAD
		public EndpointCallExecutablesBuilder add(EndpointCallExecutableProvider endpointCallExecutableProvider) {
			providers.add(endpointCallExecutableProvider);
			return this;
		}

		public EndpointCallExecutablesBuilder add(EndpointCallExecutableProvider...providers) {
=======
		public EndpointCallHandlersBuilder add(EndpointCallHandlerProvider provider) {
			providers.add(provider);
			return this;
		}

		public EndpointCallHandlersBuilder add(EndpointCallHandlerProvider...providers) {
>>>>>>> ea4d3f4... Mudança de nomes
			this.providers.addAll(Arrays.asList(providers));
			return this;
		}

<<<<<<< HEAD
		public DiscoveryComponentConfigurationBuilder<EndpointCallExecutablesBuilder> discovery() {
=======
		public DiscoveryComponentConfigurationBuilder<EndpointCallHandlersBuilder> discovery() {
>>>>>>> ea4d3f4... Mudança de nomes
			return discoveryComponentConfiguration;
		}

		public RestifyProxyBuilder and() {
			return context;
		}

<<<<<<< HEAD
		private EndpointCallExecutables build() {
			Collection<EndpointCallExecutableProvider> all = new ArrayList<>();
=======
		private EndpointCallHandlers build() {
			Collection<EndpointCallHandlerProvider> all = new ArrayList<>();
>>>>>>> ea4d3f4... Mudança de nomes

			all.addAll(providers);
			all.addAll(built);
			all.addAll(async.build());

<<<<<<< HEAD
			if (discoveryComponentConfiguration.enabled) all.addAll(provider.all(EndpointCallExecutableProvider.class));

			return new EndpointCallExecutables(all);
		}
	}

	private class AsyncEndpointCallExecutablesBuilder {

		private final Collection<EndpointCallExecutableProvider> providers = new ArrayList<>();

		private Executor executor = ExecutorAsyncEndpointCall.pool();

		private AsyncEndpointCallExecutablesBuilder all() {
			providers.add(new FutureEndpointCallExecutableAdapter<Object, Object>(executor));
			providers.add(new CompletionStageEndpointCallExecutableAdapter<Object, Object>(executor));
			providers.add(new CompletionStageCallbackEndpointCallExecutableAdapter<Object, Object>(executor));
			providers.add(new AsyncEndpointCallObjectExecutableAdapter<Object, Object>(executor));
			providers.add(new AsyncCallbackEndpointCallExecutableAdapter<Object, Object>(executor));

			if (executor instanceof ExecutorService) {
				providers.add(new FutureTaskEndpointCallExecutableAdapter<Object, Object>((ExecutorService) executor));
=======
			if (discoveryComponentConfiguration.enabled) all.addAll(provider.all(EndpointCallHandlerProvider.class));

			return new EndpointCallHandlers(all);
		}
	}

	private class AsyncEndpointCallHandlerBuilder {

		private final Collection<EndpointCallHandlerProvider> providers = new ArrayList<>();

		private Executor executor = ExecutorAsyncEndpointCall.pool();

		private AsyncEndpointCallHandlerBuilder all() {
			providers.add(new FutureEndpointCallHandlerAdapter<Object, Object>(executor));
			providers.add(new CompletionStageEndpointCallHandlerAdapter<Object, Object>(executor));
			providers.add(new CompletionStageCallbackEndpointCallHandlerAdapter<Object, Object>(executor));
			providers.add(new AsyncEndpointCallObjectHandlerAdapter<Object, Object>(executor));
			providers.add(new AsyncCallbackEndpointCallHandlerAdapter<Object, Object>(executor));

			if (executor instanceof ExecutorService) {
				providers.add(new FutureTaskEndpointCallHandlerAdapter<Object, Object>((ExecutorService) executor));
>>>>>>> ea4d3f4... Mudança de nomes
			}

			return this;
		}

<<<<<<< HEAD
		private AsyncEndpointCallExecutablesBuilder with(Executor executor) {
=======
		private AsyncEndpointCallHandlerBuilder with(Executor executor) {
>>>>>>> ea4d3f4... Mudança de nomes
			this.executor = executor;
			return this;
		}

<<<<<<< HEAD
		private Collection<EndpointCallExecutableProvider> build() {
=======
		private Collection<EndpointCallHandlerProvider> build() {
>>>>>>> ea4d3f4... Mudança de nomes
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
		private final HttpClientRequestConfiguration.Builder httpClientRequestConfigurationBuilder = new HttpClientRequestConfiguration.Builder();
		private final HttpClientRequestInterceptorsBuilder interceptors = new HttpClientRequestInterceptorsBuilder();

		private HttpClientRequestConfiguration httpClientRequestConfiguration = null;

		private HttpClientRequestConfigurationBuilder(RestifyProxyBuilder context) {
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
		
		public RestifyProxyBuilder interceptors(HttpClientRequestInterceptor...interceptors) {
			this.interceptors.add(interceptors);
			return context;
		}

		public RestifyProxyBuilder using(HttpClientRequestConfiguration httpClientRequestConfiguration) {
			this.httpClientRequestConfiguration = httpClientRequestConfiguration;
			return context;
		}

		public RestifyProxyBuilder and() {
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

			public RestifyProxyBuilder and() {
				return context;
			}
		}

		public class HttpClientRequestInterceptorsBuilder {

			private final Collection<HttpClientRequestInterceptor> all = new ArrayList<>();

			public HttpClientRequestInterceptorsBuilder add(HttpClientRequestInterceptor... interceptors) {
				this.all.addAll(Arrays.asList(interceptors));
				return this;
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

		public RestifyProxyBuilder disabled() {
			this.enabled = false;
			return context;
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
