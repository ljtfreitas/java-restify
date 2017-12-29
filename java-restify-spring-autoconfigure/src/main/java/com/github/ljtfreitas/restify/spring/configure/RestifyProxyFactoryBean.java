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
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.FactoryBean;

import com.github.ljtfreitas.restify.http.RestifyProxyBuilder;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.authentication.Authentication;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractReader;

public class RestifyProxyFactoryBean implements FactoryBean<Object> {

	private Class<?> objectType;

	private URL endpoint;

	private HttpClientRequestFactory httpClientRequestFactory;

	private ContractReader restifyContractReader;

	private EndpointRequestExecutor endpointRequestExecutor;

	private Collection<EndpointRequestInterceptor> interceptors = new ArrayList<>();

	private Collection<HttpMessageConverter> converters = new ArrayList<>();

	private Collection<EndpointCallExecutableProvider> executables = new ArrayList<>();

	private Authentication authentication;

	private EndpointResponseErrorFallback endpointResponseErrorFallback;

	private ExecutorService asyncExecutorService;

	@Override
	public Object getObject() throws Exception {
		RestifyProxyBuilder builder = new RestifyProxyBuilder();

		builder.client(httpClientRequestFactory)
				.contract(restifyContractReader)
				.executor(endpointRequestExecutor)
				.executables()
					.add(executables())
					.async(asyncExecutorService)
					.and()
				.converters(converters())
				.error(endpointResponseErrorFallback)
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

	private EndpointCallExecutableProvider[] executables() {
		return executables.toArray(new EndpointCallExecutableProvider[0]);
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

	public void setRestifyContractReader(ContractReader restifyContractReader) {
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

	public void setExecutables(Collection<EndpointCallExecutableProvider> executables) {
		this.executables = executables;
	}

	public void setEndpointResponseErrorFallback(EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this.endpointResponseErrorFallback = endpointResponseErrorFallback;
	}

	public void setAsyncExecutorService(ExecutorService asyncExecutorService) {
		this.asyncExecutorService = asyncExecutorService;
	}
}
