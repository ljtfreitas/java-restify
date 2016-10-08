package com.restify.http.spring.autoconfigure;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.FactoryBean;

import com.restify.http.RestifyProxyBuilder;
import com.restify.http.client.authentication.Authentication;
import com.restify.http.client.message.HttpMessageConverter;
import com.restify.http.client.request.EndpointRequestExecutor;
import com.restify.http.client.request.HttpClientRequestFactory;
import com.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.restify.http.contract.metadata.RestifyContractReader;

class RestifyProxyFactoryBean implements FactoryBean<Object> {

	private Class<?> objectType;

	private URL endpoint;

	private RestifyContractReader restifyContractReader;

	private Collection<HttpMessageConverter> converters = new ArrayList<>();

	private EndpointRequestExecutor endpointRequestExecutor;

	private Collection<EndpointRequestInterceptor> interceptors = new ArrayList<>();

	private Authentication authentication;

	private HttpClientRequestFactory httpClientRequestFactory;

	@Override
	public Object getObject() throws Exception {
		RestifyProxyBuilder builder = new RestifyProxyBuilder();

		builder.client(httpClientRequestFactory)
				.contract(restifyContractReader)
				.executor(endpointRequestExecutor)
				.converters(converters())
				.interceptors(interceptors());

		if (authentication != null) {
			builder.interceptors().authentication(authentication);
		}

		return builder.target(objectType, endpoint()).build();
	}

	private String endpoint() {
		return endpoint == null ? null : endpoint.toString();
	}

	private EndpointRequestInterceptor[] interceptors() {
		return interceptors.toArray(new EndpointRequestInterceptor[0]);
	}

	private HttpMessageConverter[] converters() {
		return converters.toArray(new HttpMessageConverter[0]);
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

	public void setRestifyContractReader(RestifyContractReader restifyContractReader) {
		this.restifyContractReader = restifyContractReader;
	}

	public void setConverters(Collection<HttpMessageConverter> converters) {
		this.converters = converters;
	}

	public void setEndpointRequestExecutor(EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequestExecutor = endpointRequestExecutor;
	}

	public void setInterceptors(Collection<EndpointRequestInterceptor> interceptors) {
		this.interceptors = interceptors;
	}

	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}

	public void setHttpClientRequestFactory(HttpClientRequestFactory httpClientRequestFactory) {
		this.httpClientRequestFactory = httpClientRequestFactory;
	}
}
