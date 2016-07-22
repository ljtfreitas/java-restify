package com.restify.http;

import java.lang.reflect.Proxy;
import java.util.Optional;

import com.restify.http.client.EndpointMethodExecutor;
import com.restify.http.client.EndpointRequestExecutor;
import com.restify.http.client.HttpClientRequestFactory;
import com.restify.http.client.RestifyEndpointRequestExecutor;
import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.client.converter.HttpMessageConverters;
import com.restify.http.client.jdk.JdkHttpClientRequestFactory;
import com.restify.http.contract.DefaultRestifyContract;
import com.restify.http.contract.RestifyContract;
import com.restify.http.metadata.EndpointTarget;

public class RestifyProxyBuilder {

	private RestifyContract contract;

	private HttpClientRequestFactory httpClientRequestFactory;

	private HttpMessageConverters messageConverters;

	private EndpointRequestExecutor endpointRequestExecutor;

	public RestifyProxyBuilder client(HttpClientRequestFactory httpClientRequestFactory) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		return this;
	}

	public RestifyProxyBuilder contract(RestifyContract contract) {
		this.contract = contract;
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

			Class<?>[] types = new Class<?>[] {type};
			return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), types, restifyProxyHandler);
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
			return Optional.ofNullable(endpointRequestExecutor)
					.orElseGet(() -> new RestifyEndpointRequestExecutor(httpClientRequestFactory(), messageConverters()));
		}

		private HttpMessageConverters messageConverters() {
			return Optional.ofNullable(messageConverters)
					.orElseGet(() -> new HttpMessageConverters());
		}

		private HttpClientRequestFactory httpClientRequestFactory() {
			return Optional.ofNullable(httpClientRequestFactory)
					.orElseGet(() -> new JdkHttpClientRequestFactory());
		}
	}
}
