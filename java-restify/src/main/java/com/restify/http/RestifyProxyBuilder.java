package com.restify.http;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Optional;

import com.restify.http.client.EndpointMethodExecutor;
import com.restify.http.client.authentication.Authentication;
import com.restify.http.client.message.HttpMessageConverter;
import com.restify.http.client.message.HttpMessageConverters;
import com.restify.http.client.request.EndpointRequestExecutor;
import com.restify.http.client.request.EndpointRequestWriter;
import com.restify.http.client.request.HttpClientRequestFactory;
import com.restify.http.client.request.RestifyEndpointRequestExecutor;
import com.restify.http.client.request.interceptor.AcceptHeaderEndpointRequestInterceptor;
import com.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;
import com.restify.http.client.request.interceptor.authentication.AuthenticationEndpoinRequestInterceptor;
import com.restify.http.client.request.jdk.JdkHttpClientRequestFactory;
import com.restify.http.client.response.EndpointResponseReader;
import com.restify.http.contract.DefaultRestifyContract;
import com.restify.http.contract.RestifyContract;
import com.restify.http.contract.metadata.DefaultRestifyContractReader;
import com.restify.http.contract.metadata.EndpointTarget;
import com.restify.http.contract.metadata.RestifyContractReader;

public class RestifyProxyBuilder {

	private RestifyContractReader contractReader;

	private HttpClientRequestFactory httpClientRequestFactory;

	private HttpMessageConverters messageConverters;

	private EndpointRequestExecutor endpointRequestExecutor;

	private EndpointRequestInterceptorStack endpointRequestInterceptorStack = new EndpointRequestInterceptorStack();

	public RestifyProxyBuilder client(HttpClientRequestFactory httpClientRequestFactory) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		return this;
	}

	public RestifyProxyBuilder contract(RestifyContractReader contract) {
		this.contractReader = contract;
		return this;
	}

	public RestifyProxyBuilder converters(HttpMessageConverter...converters) {
		this.messageConverters = new HttpMessageConverters(converters);
		return this;
	}

	public RestifyProxyBuilder executor(EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequestExecutor = endpointRequestExecutor;
		return this;
	}

	public RestifyProxyBuilder interceptor(EndpointRequestInterceptor interceptor) {
		endpointRequestInterceptorStack.add(interceptor);
		return this;
	}

	public RestifyProxyBuilder interceptors(EndpointRequestInterceptor...interceptors) {
		Arrays.stream(interceptors).forEach(endpointRequestInterceptorStack::add);
		return this;
	}

	public RestifyProxyBuilder authentication(Authentication authentication) {
		endpointRequestInterceptorStack.add(new AuthenticationEndpoinRequestInterceptor(authentication));
		return this;
	}

	public <T> RestifyProxyBuilderOnTarget<T> target(Class<T> target) {
		return new RestifyProxyBuilderOnTarget<>(target, null);
	}

	public <T> RestifyProxyBuilderOnTarget<T> target(Class<T> target, String endpoint) {
		return new RestifyProxyBuilderOnTarget<>(target, endpoint);
	}

	public class RestifyProxyBuilderOnTarget<T> {
		private final Class<T> type;
		private final String endpoint;

		private RestifyProxyBuilderOnTarget(Class<T> type, String endpoint) {
			this.type = type;
			this.endpoint = endpoint;
		}

		@SuppressWarnings("unchecked")
		public T build() {
			RestifyProxyHandler restifyProxyHandler = doBuild();

			return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{type}, restifyProxyHandler);
		}

		private RestifyProxyHandler doBuild() {
			EndpointTarget target = new EndpointTarget(type, endpoint);

			EndpointMethodExecutor endpointMethodExecutor = new EndpointMethodExecutor(endpointRequestExecutor());

			return new RestifyProxyHandler(contract().read(target), endpointMethodExecutor);
		}

		private RestifyContract contract() {
			return Optional.ofNullable(contractReader)
					.map(c -> new DefaultRestifyContract(c))
						.orElseGet(() -> new DefaultRestifyContract(new DefaultRestifyContractReader()));
		}

		private EndpointRequestExecutor endpointRequestExecutor() {
			HttpMessageConverters messageConverters = messageConverters();
			return Optional.ofNullable(endpointRequestExecutor)
					.orElseGet(() -> new RestifyEndpointRequestExecutor(httpClientRequestFactory(), endpointRequestInterceptorStack(), 
							new EndpointRequestWriter(messageConverters), new EndpointResponseReader(messageConverters)));
		}

		private EndpointRequestInterceptorStack endpointRequestInterceptorStack() {
			endpointRequestInterceptorStack.add(new AcceptHeaderEndpointRequestInterceptor(messageConverters()));
			return endpointRequestInterceptorStack;
		}

		private HttpMessageConverters messageConverters() {
			return Optional.ofNullable(messageConverters)
					.orElseGet(() -> HttpMessageConverters.build());
		}

		private HttpClientRequestFactory httpClientRequestFactory() {
			return Optional.ofNullable(httpClientRequestFactory)
					.orElseGet(() -> new JdkHttpClientRequestFactory());
		}
	}
}
