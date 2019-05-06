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
package com.github.ljtfreitas.restify.cdi;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.AnnotationLiteral;

import com.github.ljtfreitas.restify.http.RestifyProxyBuilder;
import com.github.ljtfreitas.restify.http.client.HttpClientRequestConfiguration;
import com.github.ljtfreitas.restify.http.client.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerProvider;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.authentication.Authentication;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.HttpClientRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.retry.RetryConfiguration;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractExpressionResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractReader;
import com.github.ljtfreitas.restify.util.async.DisposableExecutors;

class RestifyProxyCdiBeanFactory {

	private final Class<?> javaType;
	private final Restifyable restifyable;
	private final Collection<RestifyProxyConfiguration> configurations;
	
	public RestifyProxyCdiBeanFactory(Class<?> javaType) {
		this.javaType = javaType;
		this.restifyable = javaType.getAnnotation(Restifyable.class);
		this.configurations = configurationsOf(restifyable.configuration());
	}

	private Collection<RestifyProxyConfiguration> configurationsOf(Class<? extends RestifyProxyConfiguration>[] configurations) {
		return Arrays.stream(configurations)
				.map(c -> get(c))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public Object create() {
		RestifyProxyBuilder builder = new RestifyProxyBuilder();

		builder
			.client()
				.using(httpClientRequestFactory())
				.interceptors(httpClientRequestInterceptors())
				.configure()
					.using(httpClientRequestConfiguration())
				.and()
			.contract()
				.using(contractReader())
				.resolver(contractExpressionResolver())
				.and()
			.executor()
				.using(endpointRequestExecutor())
				.interceptors(endpointRequestInterceptors())
				.and()
			.retry()
				.enabled(withRetry())
				.using(retry())
			.handlers()
				.add(handlers())
				.discovery()
					.disabled()
				.and()
			.converters()
				.wildcard()
				.add(converters())
				.discovery()
					.disabled()
				.and()
			.async(asyncExecutorService())
			.error(endpointResponseErrorFallback());

		authentication()
			.ifPresent(a -> builder
								.executor()
									.interceptors()
										.authentication(a));

		return builder.target(javaType, endpoint())
				.build();
	}

	private HttpClientRequestFactory httpClientRequestFactory() {
		return configured(RestifyProxyConfiguration::httpClientRequestFactory)
				.orElseGet(() -> get(HttpClientRequestFactory.class, () -> new JdkHttpClientRequestFactory()));
	}

	private HttpClientRequestInterceptor[] httpClientRequestInterceptors() {
		return Stream.concat(configurations.stream()
				.map(RestifyProxyConfiguration::httpClientRequestInterceptors)
				.flatMap(Collection::stream), all(HttpClientRequestInterceptor.class))
					.toArray(HttpClientRequestInterceptor[]::new);
	}

	private HttpClientRequestConfiguration httpClientRequestConfiguration() {
		return configured(RestifyProxyConfiguration::httpClientRequestConfiguration)
				.orElseGet(() -> get(HttpClientRequestConfiguration.class));
	}

	private ContractReader contractReader() {
		return configured(RestifyProxyConfiguration::contractReader)
				.orElseGet(() -> get(ContractReader.class));
	}

	private ContractExpressionResolver contractExpressionResolver() {
		return configured(RestifyProxyConfiguration::contractExpressionResolver)
				.orElseGet(() -> get(ContractExpressionResolver.class));
	}


	private EndpointRequestExecutor endpointRequestExecutor() {
		return configured(RestifyProxyConfiguration::endpointRequestExecutor)
				.orElseGet(() -> get(EndpointRequestExecutor.class));
	}

	private EndpointRequestInterceptor[] endpointRequestInterceptors() {
		return Stream.concat(configurations.stream()
				.map(RestifyProxyConfiguration::endpointRequestInterceptors)
				.flatMap(Collection::stream), all(EndpointRequestInterceptor.class))
					.toArray(EndpointRequestInterceptor[]::new);
	}

	private boolean withRetry() {
		return retry() != null;
	}

	private RetryConfiguration retry() {
		return configured(RestifyProxyConfiguration::retry)
				.orElseGet(() -> get(RetryConfiguration.class));
	}

	private EndpointCallHandlerProvider[] handlers() {
		return Stream.concat(configurations.stream()
				.map(RestifyProxyConfiguration::handlers)
				.flatMap(Collection::stream), all(EndpointCallHandlerProvider.class))
					.toArray(EndpointCallHandlerProvider[]::new);
	}

	private Executor asyncExecutorService() {
		return configured(RestifyProxyConfiguration::asyncExecutor)
				.orElseGet(() -> get(Executor.class, new AsyncAnnotationLiteral(), DisposableExecutors::newCachedThreadPool));
	}

	private HttpMessageConverter[] converters() {
		return Stream.concat(configurations.stream()
				.map(RestifyProxyConfiguration::converters)
				.flatMap(Collection::stream), all(HttpMessageConverter.class))
					.toArray(HttpMessageConverter[]::new);
	}

	private EndpointResponseErrorFallback endpointResponseErrorFallback() {
		return configured(RestifyProxyConfiguration::endpointResponseErrorFallback)
				.orElseGet(() -> get(EndpointResponseErrorFallback.class));
	}

	private Optional<Authentication> authentication() {
		return Optional.ofNullable(configured(RestifyProxyConfiguration::authentication)
				.orElseGet(() -> get(Authentication.class)));
	}

	private String endpoint() {
		return configured(RestifyProxyConfiguration::endpoint)
				.orElseGet(restifyable::endpoint);
	}

	private <T> T get(Class<? extends T> type) {
		return get(type, () -> null);
	}

	private <T> T get(Class<? extends T> type, Supplier<T> supplier) {
		Instance<? extends T> instance = CDI.current().select(type);

		return instance.isUnsatisfied() ? supplier.get() : instance.get();
	}

	private <T> T get(Class<? extends T> type, Annotation qualifier, Supplier<T> supplier) {
		Instance<? extends T> instance = CDI.current().select(type, qualifier);

		return instance.isUnsatisfied() ? supplier.get() : instance.get();
	}

	private <T> Stream<? extends T> all(Class<? extends T> type) {
		Instance<? extends T> instance = CDI.current().select(type);
		return instance.stream();
	}

	private <T> Optional<T> configured(Function<RestifyProxyConfiguration, T> function) {
		return configurations.stream()
				.map(function)
				.filter(Objects::nonNull)
				.findFirst();
	}

	@SuppressWarnings("serial")
	private static final class AsyncAnnotationLiteral extends AnnotationLiteral<Async> implements Async {
	}
}
