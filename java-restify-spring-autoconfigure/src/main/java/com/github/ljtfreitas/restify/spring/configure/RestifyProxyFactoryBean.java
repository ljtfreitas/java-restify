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
package com.github.ljtfreitas.restify.spring.configure;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.beans.factory.FactoryBean;

import com.github.ljtfreitas.restify.http.RestifyProxyBuilder;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerProvider;
import com.github.ljtfreitas.restify.http.client.jdk.HttpClientRequestConfiguration;
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

public class RestifyProxyFactoryBean implements FactoryBean<Object> {

	private Class<?> objectType;

	private URL endpoint;

	private HttpClientRequestFactory httpClientRequestFactory;

	private HttpClientRequestConfiguration httpClientRequestConfiguration;

	private Collection<HttpClientRequestInterceptor> httpClientRequestInterceptors = new ArrayList<>();

	private ContractReader contractReader;
	
	private ContractExpressionResolver contractExpressionResolver;

	private EndpointRequestExecutor endpointRequestExecutor;

	private Collection<EndpointRequestInterceptor> endpointRequestInterceptors = new ArrayList<>();

	private Collection<HttpMessageConverter> converters = new ArrayList<>();

	private Collection<EndpointCallHandlerProvider> handlers = new ArrayList<>();

	private Authentication authentication;

	private EndpointResponseErrorFallback endpointResponseErrorFallback;

	private ExecutorService asyncExecutorService;
	
	private RetryConfiguration retry;

	private Collection<RestifyProxyConfiguration> configurations = new ArrayList<>();

	@Override
	public Object getObject() throws Exception {
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
			.async(asyncExecutor())
			.error(endpointResponseErrorFallback());

		authentication()
			.ifPresent(a -> builder
								.executor()
									.interceptors()
										.authentication(a));

		return builder.target(objectType, endpoint())
				.build();
	}

	@Override
	public Class<?> getObjectType() {
		return objectType;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setObjectType(Class<?> objectType) {
		this.objectType = objectType;
	}

	public void setEndpoint(URL endpoint) {
		this.endpoint = endpoint;
	}

	public void setHttpClientRequestFactory(HttpClientRequestFactory httpClientRequestFactory) {
		this.httpClientRequestFactory = httpClientRequestFactory;
	}
	
	public void setHttpClientRequestConfiguration(HttpClientRequestConfiguration httpClientRequestConfiguration) {
		this.httpClientRequestConfiguration = httpClientRequestConfiguration;
	}

	public void setHttpClientRequestInterceptors(Collection<HttpClientRequestInterceptor> httpClientRequestInterceptors) {
		this.httpClientRequestInterceptors = httpClientRequestInterceptors;
	}

	public void setContractReader(ContractReader contractReader) {
		this.contractReader = contractReader;
	}

	public void setContractExpressionResolver(ContractExpressionResolver contractExpressionResolver) {
		this.contractExpressionResolver = contractExpressionResolver;
	}

	public void setEndpointRequestExecutor(EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequestExecutor = endpointRequestExecutor;
	}

	public void setEndpointRequestInterceptors(Collection<EndpointRequestInterceptor> endpointRequestInterceptors) {
		this.endpointRequestInterceptors = endpointRequestInterceptors;
	}

	public void setConverters(Collection<HttpMessageConverter> converters) {
		this.converters = converters;
	}

	public void setHandlers(Collection<EndpointCallHandlerProvider> handlers) {
		this.handlers = handlers;
	}

	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}

	public void setEndpointResponseErrorFallback(EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this.endpointResponseErrorFallback = endpointResponseErrorFallback;
	}

	public void setAsyncExecutorService(ExecutorService asyncExecutorService) {
		this.asyncExecutorService = asyncExecutorService;
	}

	public void setRetry(RetryConfiguration retry) {
		this.retry = retry;
	}

	public void setConfigurations(Collection<RestifyProxyConfiguration> configurations) {
		this.configurations = configurations;
	}
	
	private HttpClientRequestFactory httpClientRequestFactory() {
		return configured(RestifyProxyConfiguration::httpClientRequestFactory)
				.orElse(httpClientRequestFactory);
	}

	private HttpClientRequestConfiguration httpClientRequestConfiguration() {
		return configured(RestifyProxyConfiguration::httpClientRequestConfiguration)
				.orElse(httpClientRequestConfiguration);
	}
	
	private HttpClientRequestInterceptor[] httpClientRequestInterceptors() {
		return Stream.concat(configurations.stream()
				.map(RestifyProxyConfiguration::httpClientRequestInterceptors)
				.flatMap(Collection::stream), httpClientRequestInterceptors.stream())
					.toArray(HttpClientRequestInterceptor[]::new);
	}

	private ContractReader contractReader() {
		return configured(RestifyProxyConfiguration::contractReader)
				.orElse(contractReader);
	}

	private ContractExpressionResolver contractExpressionResolver() {
		return configured(RestifyProxyConfiguration::contractExpressionResolver)
				.orElse(contractExpressionResolver);
	}

	private EndpointRequestExecutor endpointRequestExecutor() {
		return configured(RestifyProxyConfiguration::endpointRequestExecutor)
				.orElse(endpointRequestExecutor);
	}

	private EndpointRequestInterceptor[] endpointRequestInterceptors() {
		return Stream.concat(configurations.stream()
				.map(RestifyProxyConfiguration::endpointRequestInterceptors)
				.flatMap(Collection::stream), endpointRequestInterceptors.stream())
					.toArray(EndpointRequestInterceptor[]::new);
	}

	private boolean withRetry() {
		return retry() != null;
	}
	
	private RetryConfiguration retry() {
		return configured(RestifyProxyConfiguration::retry)
				.orElse(retry);
	}

	private EndpointCallHandlerProvider[] handlers() {
		return Stream.concat(configurations.stream()
				.map(RestifyProxyConfiguration::handlers)
				.flatMap(Collection::stream), handlers.stream())
					.toArray(EndpointCallHandlerProvider[]::new);
	}

	private Executor asyncExecutor() {
		return configured(RestifyProxyConfiguration::asyncExecutor)
				.orElse(asyncExecutorService);
	}

	private HttpMessageConverter[] converters() {
		return Stream.concat(configurations.stream()
				.map(RestifyProxyConfiguration::converters)
				.flatMap(Collection::stream), converters.stream())
					.toArray(HttpMessageConverter[]::new);
	}

	private EndpointResponseErrorFallback endpointResponseErrorFallback() {
		return configured(RestifyProxyConfiguration::endpointResponseErrorFallback)
				.orElse(endpointResponseErrorFallback);
	}

	private Optional<Authentication> authentication() {
		return Optional.ofNullable(configured(RestifyProxyConfiguration::authentication)
				.orElse(authentication));
	}

	private String endpoint() {
		return endpoint == null ? null : endpoint.toString();
	}

	private <T> Optional<T> configured(Function<RestifyProxyConfiguration, T> function) {
		return configurations.stream()
				.map(function)
				.filter(Objects::nonNull)
				.findFirst();
	}
}
