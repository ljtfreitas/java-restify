package com.restify.http;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import com.restify.http.client.EndpointMethodExecutor;
import com.restify.http.client.authentication.Authentication;
import com.restify.http.client.call.EndpointCallFactory;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.client.call.exec.EndpointCallExecutables;
import com.restify.http.client.message.HttpMessageConverter;
import com.restify.http.client.message.HttpMessageConverters;
import com.restify.http.client.message.converter.json.JsonMessageConverter;
import com.restify.http.client.message.converter.text.ScalarMessageConverter;
import com.restify.http.client.message.converter.text.TextHtmlMessageConverter;
import com.restify.http.client.message.converter.text.TextPlainMessageConverter;
import com.restify.http.client.message.converter.xml.JaxbXmlMessageConverter;
import com.restify.http.client.message.form.FormURLEncodedFormObjectMessageConverter;
import com.restify.http.client.message.form.FormURLEncodedMapMessageConverter;
import com.restify.http.client.message.form.FormURLEncodedParametersMessageConverter;
import com.restify.http.client.message.form.multipart.MultipartFormFileObjectMessageWriter;
import com.restify.http.client.message.form.multipart.MultipartFormMapMessageWriter;
import com.restify.http.client.message.form.multipart.MultipartFormObjectMessageWriter;
import com.restify.http.client.message.form.multipart.MultipartFormParametersMessageWriter;
import com.restify.http.client.request.EndpointRequestExecutor;
import com.restify.http.client.request.EndpointRequestFactory;
import com.restify.http.client.request.EndpointRequestWriter;
import com.restify.http.client.request.HttpClientRequestFactory;
import com.restify.http.client.request.RestifyEndpointRequestExecutor;
import com.restify.http.client.request.interceptor.AcceptHeaderEndpointRequestInterceptor;
import com.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;
import com.restify.http.client.request.interceptor.authentication.AuthenticationEndpoinRequestInterceptor;
import com.restify.http.client.request.jdk.JdkHttpClientRequestFactory;
import com.restify.http.client.response.EndpointResponseReader;
import com.restify.http.contract.ContentType;
import com.restify.http.contract.DefaultRestifyContract;
import com.restify.http.contract.RestifyContract;
import com.restify.http.contract.metadata.DefaultRestifyContractReader;
import com.restify.http.contract.metadata.EndpointTarget;
import com.restify.http.contract.metadata.RestifyContractReader;

public class RestifyProxyBuilder {

	private RestifyContractReader contractReader;

	private HttpClientRequestFactory httpClientRequestFactory;

	private EndpointRequestExecutor endpointRequestExecutor;

	private HttpMessageConvertersBuilder httpMessageConvertersBuilder = new HttpMessageConvertersBuilder(this);

	private EndpointRequestInterceptorsBuilder endpointRequestInterceptorsBuilder = new EndpointRequestInterceptorsBuilder(this);

	private EndpointCallExecutablesBuilder endpointMethodExecutablesBuilder = new EndpointCallExecutablesBuilder(this);

	public RestifyProxyBuilder client(HttpClientRequestFactory httpClientRequestFactory) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		return this;
	}

	public RestifyProxyBuilder contract(RestifyContractReader contract) {
		this.contractReader = contract;
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

	public RestifyProxyBuilder executables(EndpointCallExecutableFactory<?, ?>...factories) {
		this.endpointMethodExecutablesBuilder.add(factories);
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

			EndpointMethodExecutor endpointMethodExecutor = new EndpointMethodExecutor(endpointMethodExecutables(), endpointMethodCallFactory()); 

			RestifyContract restifyContract = contract();

			return new RestifyProxyHandler(restifyContract.read(target), endpointMethodExecutor);
		}

		private EndpointCallExecutables endpointMethodExecutables() {
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
							new EndpointRequestWriter(messageConverters), new EndpointResponseReader(messageConverters)));
		}

		private HttpClientRequestFactory httpClientRequestFactory() {
			return Optional.ofNullable(httpClientRequestFactory)
					.orElseGet(() -> new JdkHttpClientRequestFactory());
		}

		private RestifyContract contract() {
			return Optional.ofNullable(contractReader)
					.map(c -> new DefaultRestifyContract(c))
					.orElseGet(() -> new DefaultRestifyContract(new DefaultRestifyContractReader()));
		}
	}

	public class HttpMessageConvertersBuilder {

		private final RestifyProxyBuilder context;
		private final Collection<HttpMessageConverter> converters = new ArrayList<>();

		private HttpMessageConvertersBuilder(RestifyProxyBuilder context) {
			this.context = context;
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
			return json().xml().text().form();
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
		private final Collection<EndpointCallExecutableFactory<?, ?>> factories = new ArrayList<>();

		private EndpointCallExecutablesBuilder(RestifyProxyBuilder context) {
			this.context = context;
		}

		public EndpointCallExecutablesBuilder add(EndpointCallExecutableFactory<?, ?> endpointMethodExecutableFactory) {
			factories.add(endpointMethodExecutableFactory);
			return this;
		}

		public EndpointCallExecutablesBuilder add(EndpointCallExecutableFactory<?, ?>...factories) {
			this.factories.addAll(Arrays.asList(factories));
			return this;
		}

		public RestifyProxyBuilder and() {
			return context;
		}

		private EndpointCallExecutables build() {
			return new EndpointCallExecutables(factories);
		}
	}
}
