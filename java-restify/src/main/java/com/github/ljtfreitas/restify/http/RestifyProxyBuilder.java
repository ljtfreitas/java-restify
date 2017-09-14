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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import com.github.ljtfreitas.restify.http.client.EndpointMethodExecutor;
import com.github.ljtfreitas.restify.http.client.authentication.Authentication;
import com.github.ljtfreitas.restify.http.client.call.EndpointCallFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutables;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallObjectExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.HeadersEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncCallbackEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutableFactory;
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
import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.ByteArrayMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.InputStreamMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.octet.OctetByteArrayMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.octet.OctetInputStreamMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.octet.OctetSerializableMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.ScalarMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextHtmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextPlainMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.xml.JaxbXmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.form.FormURLEncodedFormObjectMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.form.FormURLEncodedMapMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.form.FormURLEncodedParametersMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.form.multipart.MultipartFormFileObjectMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.form.multipart.MultipartFormMapMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.form.multipart.MultipartFormObjectMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.form.multipart.MultipartFormParametersMessageWriter;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.request.EndpointVersion;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.RestifyEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.AcceptHeaderEndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.AcceptVersionHeaderEndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.ContentTypeHeaderEndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;
import com.github.ljtfreitas.restify.http.client.request.interceptor.authentication.AuthenticationEndpoinRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.jdk.HttpClientRequestConfiguration;
import com.github.ljtfreitas.restify.http.client.request.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.response.DefaultEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.http.contract.ContentType;
import com.github.ljtfreitas.restify.http.contract.DefaultRestifyContract;
import com.github.ljtfreitas.restify.http.contract.RestifyContract;
import com.github.ljtfreitas.restify.http.contract.metadata.DefaultRestifyContractReader;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointTarget;
import com.github.ljtfreitas.restify.http.contract.metadata.RestifyContractExpressionResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.RestifyContractReader;
import com.github.ljtfreitas.restify.http.contract.metadata.SimpleRestifyContractExpressionResolver;

public class RestifyProxyBuilder {

	private RestifyContractReader contractReader;

	private RestifyContractExpressionResolver expressionResolver;

	private HttpClientRequestFactory httpClientRequestFactory;

	private EndpointRequestExecutor endpointRequestExecutor;

	private HttpMessageConvertersBuilder httpMessageConvertersBuilder = new HttpMessageConvertersBuilder(this);

	private EndpointRequestInterceptorsBuilder endpointRequestInterceptorsBuilder = new EndpointRequestInterceptorsBuilder(this);

	private EndpointCallExecutablesBuilder endpointMethodExecutablesBuilder = new EndpointCallExecutablesBuilder(this);

	private EndpointResponseErrorFallbackBuilder endpointResponseErrorFallbackBuilder = new EndpointResponseErrorFallbackBuilder(this);

	private HttpClientRequestConfigurationBuilder httpClientRequestConfigurationBuilder = new HttpClientRequestConfigurationBuilder(this);

	public RestifyProxyBuilder client(HttpClientRequestFactory httpClientRequestFactory) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		return this;
	}

	public HttpClientRequestConfigurationBuilder client() {
		return httpClientRequestConfigurationBuilder;
	}

	public RestifyProxyBuilder contract(RestifyContractReader contract) {
		this.contractReader = contract;
		return this;
	}

	public RestifyProxyBuilder expression(RestifyContractExpressionResolver expression) {
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
		return this.endpointMethodExecutablesBuilder;
	}

	public RestifyProxyBuilder executables(EndpointCallExecutableProvider providers) {
		this.endpointMethodExecutablesBuilder.add(providers);
		return this;
	}

	public EndpointResponseErrorFallbackBuilder error() {
		return endpointResponseErrorFallbackBuilder;
	}

	public RestifyProxyBuilder error(EndpointResponseErrorFallback fallback) {
		this.endpointResponseErrorFallbackBuilder = new EndpointResponseErrorFallbackBuilder(this, fallback);
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

			return new ProxyFactory(restifyProxyHandler).create(type);
		}

		private RestifyProxyHandler doBuild() {
			EndpointTarget target = new EndpointTarget(type, endpoint);

			EndpointMethodExecutor endpointMethodExecutor = new EndpointMethodExecutor(endpointCallExecutables(), endpointMethodCallFactory()); 

			RestifyContract restifyContract = contract();

			return new RestifyProxyHandler(restifyContract.read(target), endpointMethodExecutor);
		}

		private EndpointCallExecutables endpointCallExecutables() {
			return endpointMethodExecutablesBuilder.build();
		}

		private EndpointCallFactory endpointMethodCallFactory() {
			return new EndpointCallFactory(endpointRequestFactory(), endpointRequestExecutor());
		}

		private EndpointRequestFactory endpointRequestFactory() {
			return new EndpointRequestFactory(endpointRequestInterceptorsBuilder.build());
		}

		private EndpointRequestExecutor endpointRequestExecutor() {
			HttpMessageConverters messageConverters = httpMessageConvertersBuilder.build();
			return Optional.ofNullable(endpointRequestExecutor)
					.orElseGet(() -> new RestifyEndpointRequestExecutor(httpClientRequestFactory(), 
							new EndpointRequestWriter(messageConverters),
							new EndpointResponseReader(messageConverters, endpointResponseErrorFallbackBuilder())));
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

		private RestifyContract contract() {
			return Optional.ofNullable(contractReader)
					.map(c -> new DefaultRestifyContract(c))
					.orElseGet(() -> new DefaultRestifyContract(new DefaultRestifyContractReader(expressionResolver())));
		}

		private RestifyContractExpressionResolver expressionResolver() {
			return Optional.ofNullable(expressionResolver)
					.orElseGet(() -> new SimpleRestifyContractExpressionResolver());
		}
	}

	public class HttpMessageConvertersBuilder {

		private final RestifyProxyBuilder context;
		private final Collection<HttpMessageConverter> converters = new ArrayList<>();

		private HttpMessageConvertersBuilder(RestifyProxyBuilder context) {
			this.context = context;
		}

		public HttpMessageConvertersBuilder wildcard() {
			converters.add(new InputStreamMessageConverter());
			converters.add(new ByteArrayMessageConverter());
			return this;
		}

		public HttpMessageConvertersBuilder wildcard(int bufferSize) {
			converters.add(new InputStreamMessageConverter(bufferSize));
			converters.add(new ByteArrayMessageConverter(bufferSize));
			return this;
		}

		public HttpMessageConvertersBuilder octetStream() {
			converters.add(new OctetInputStreamMessageConverter());
			converters.add(new OctetByteArrayMessageConverter());
			converters.add(new OctetSerializableMessageConverter<>());
			return this;
		}

		public HttpMessageConvertersBuilder octetStream(int bufferSize) {
			converters.add(new OctetInputStreamMessageConverter(bufferSize));
			converters.add(new OctetByteArrayMessageConverter(bufferSize));
			converters.add(new OctetSerializableMessageConverter<>());
			return this;
		}

		public HttpMessageConvertersBuilder json() {
			converters.add(JsonMessageConverter.available());
			return this;
		}

		public HttpMessageConvertersBuilder xml() {
			converters.add(new JaxbXmlMessageConverter<Object>());
			return this;
		}

		public HttpMessageConvertersBuilder text() {
			converters.add(new TextPlainMessageConverter());
			converters.add(new TextHtmlMessageConverter());
			converters.add(new ScalarMessageConverter());
			return this;
		}

		public HttpMessageConvertersBuilder form() {
			converters.add(new FormURLEncodedParametersMessageConverter());
			converters.add(new FormURLEncodedFormObjectMessageConverter());
			converters.add(new FormURLEncodedMapMessageConverter());
			converters.add(new MultipartFormParametersMessageWriter());
			converters.add(new MultipartFormObjectMessageWriter());
			converters.add(new MultipartFormFileObjectMessageWriter());
			converters.add(new MultipartFormMapMessageWriter());
			return this;
		}

		public HttpMessageConvertersBuilder all() {
			return wildcard().json().xml().text().form().octetStream();
		}

		public HttpMessageConvertersBuilder add(HttpMessageConverter...converters) {
			this.converters.addAll(Arrays.asList(converters));
			return this;
		}

		public RestifyProxyBuilder and() {
			return context;
		}

		private HttpMessageConverters build() {
			return converters.isEmpty() ? all().build() : new HttpMessageConverters(converters);
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

		public EndpointRequestInterceptorsBuilder accept(String... contentTypes) {
			interceptors.add(new AcceptHeaderEndpointRequestInterceptor(contentTypes));
			return this;
		}

		public EndpointRequestInterceptorsBuilder accept(ContentType... contentTypes) {
			interceptors.add(new AcceptHeaderEndpointRequestInterceptor(contentTypes));
			return this;
		}

		public EndpointRequestInterceptorsBuilder contentType(String contentType) {
			interceptors.add(new ContentTypeHeaderEndpointRequestInterceptor(contentType));
			return this;
		}

		public EndpointRequestInterceptorsBuilder contentType(ContentType contentType) {
			interceptors.add(new ContentTypeHeaderEndpointRequestInterceptor(contentType));
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

		private EndpointCallExecutablesBuilder(RestifyProxyBuilder context) {
			this.context = context;
			this.built.add(new OptionalEndpointCallExecutableFactory<Object>());
			this.built.add(new CallableEndpointCallExecutableFactory<Object, Object>());
			this.built.add(new RunnableEndpointCallExecutableFactory());
			this.built.add(new CollectionEndpointCallExecutableFactory<>());
			this.built.add(new EnumerationEndpointCallExecutableFactory<>());
			this.built.add(new IteratorEndpointCallExecutableFactory<>());
			this.built.add(new ListIteratorEndpointCallExecutableFactory<>());
			this.built.add(new IterableEndpointCallExecutableFactory<>());
			this.built.add(new EndpointCallObjectExecutableFactory<Object, Object>());
			this.built.add(new HeadersEndpointCallExecutableFactory());
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

		public RestifyProxyBuilder and() {
			return context;
		}

		private EndpointCallExecutables build() {
			providers.addAll(built);
			providers.addAll(async.build());
			return new EndpointCallExecutables(providers);
		}
	}

	private class AsyncEndpointCallExecutablesBuilder {

		private final Collection<EndpointCallExecutableProvider> providers = new ArrayList<>();

		private AsyncEndpointCallExecutablesBuilder all() {
			with(Executors.newCachedThreadPool());
			return this;
		}

		private AsyncEndpointCallExecutablesBuilder with(ExecutorService executor) {
			with((Executor) executor);
			providers.add(new FutureEndpointCallExecutableFactory<Object, Object>(executor));
			providers.add(new FutureTaskEndpointCallExecutableFactory<Object, Object>(executor));
			return this;
		}

		private AsyncEndpointCallExecutablesBuilder with(Executor executor) {
			providers.add(new CompletableFutureEndpointCallExecutableFactory<Object, Object>(executor));
			providers.add(new CompletableFutureCallbackEndpointCallExecutableFactory<Object, Object>(executor));
			providers.add(new AsyncEndpointCallExecutableFactory<Object, Object>(executor));
			providers.add(new AsyncCallbackEndpointCallExecutableFactory<Object, Object>(executor));
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
}
