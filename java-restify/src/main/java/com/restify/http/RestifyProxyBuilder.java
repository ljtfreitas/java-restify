package com.restify.http;

import java.lang.reflect.Proxy;
import java.util.Optional;

import com.restify.http.client.RestifyEndpointRequestExecutor;
import com.restify.http.client.EndpointMethodExecutor;
import com.restify.http.client.EndpointRequestExecutor;
import com.restify.http.client.EndpointRequestWriter;
import com.restify.http.client.EndpointResponseReader;
import com.restify.http.client.HttpClientRequestFactory;
import com.restify.http.client.authentication.Authentication;
import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.client.converter.HttpMessageConverters;
import com.restify.http.client.interceptor.AcceptHeaderEndpointRequestInterceptor;
import com.restify.http.client.interceptor.EndpointRequestInterceptorStack;
import com.restify.http.client.interceptor.authentication.EndpoinRequestAuthenticationInterceptor;
import com.restify.http.client.jdk.JdkHttpClientRequestFactory;
import com.restify.http.contract.DefaultRestifyContract;
import com.restify.http.contract.RestifyContract;
import com.restify.http.metadata.EndpointTarget;

public class RestifyProxyBuilder {

	private RestifyContract contract;

	private HttpClientRequestFactory httpClientRequestFactory;

	private HttpMessageConverters messageConverters;

	private EndpointRequestExecutor endpointRequestExecutor;

	private EndpointRequestInterceptorStack endpointRequestInterceptorStack = new EndpointRequestInterceptorStack();

	public RestifyProxyBuilder client(HttpClientRequestFactory httpClientRequestFactory) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		return this;
	}

	public RestifyProxyBuilder contract(RestifyContract contract) {
		this.contract = contract;
		return this;
	}

	public RestifyProxyBuilder converters(HttpMessageConverter<?>...converters) {
		this.messageConverters = new HttpMessageConverters(converters);
		return this;
	}

	public RestifyProxyBuilder executor(EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequestExecutor = endpointRequestExecutor;
		return this;
	}

	public RestifyProxyBuilder authentication(Authentication authentication) {
		endpointRequestInterceptorStack.add(new EndpoinRequestAuthenticationInterceptor(authentication));
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
			return Optional.ofNullable(contract)
					.orElseGet(() -> new DefaultRestifyContract());
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
