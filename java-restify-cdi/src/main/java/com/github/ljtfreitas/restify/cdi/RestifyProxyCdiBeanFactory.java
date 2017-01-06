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

import static com.github.ljtfreitas.restify.http.util.Preconditions.isTrue;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.AnnotationLiteral;

import com.github.ljtfreitas.restify.http.RestifyProxyBuilder;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.contract.metadata.RestifyContractExpressionResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.RestifyContractReader;

class RestifyProxyCdiBeanFactory {

	private final Class<?> javaType;
	private final Restifyable restifyable;
	
	public RestifyProxyCdiBeanFactory(Class<?> javaType) {
		this.javaType = javaType;
		this.restifyable = javaType.getAnnotation(Restifyable.class);
	}

	public Object create() {
		isTrue(restifyable.endpoint() != null && !"".equals(restifyable.endpoint()),
				"The [endpoint] attribute of the @Restifyable annotation is required.");

		RestifyProxyBuilder builder = new RestifyProxyBuilder();

		builder.client(httpClientRequestFactory())
			.contract(contractReader())
			.expression(expressionResolver())
			.executor(endpointRequestExecutor())
			.executables()
				.add(executables())
					.async(executor())
				.and()
			.converters(httpMessageConverters())
			.error(endpointResponseErrorFallback())
			.interceptors(endpointRequestInterceptors());

		return builder.target(javaType, restifyable.endpoint()).build();
	}

	private EndpointRequestInterceptor[] endpointRequestInterceptors() {
		return all(EndpointRequestInterceptor.class).toArray(new EndpointRequestInterceptor[0]);
	}

	private EndpointResponseErrorFallback endpointResponseErrorFallback() {
		return get(EndpointResponseErrorFallback.class, () -> null);
	}

	private HttpMessageConverter[] httpMessageConverters() {
		return all(HttpMessageConverter.class).toArray(new HttpMessageConverter[0]);
	}

	private Executor executor() {
		return get(Executor.class, new RestifyExecutorLiteral(), () -> Executors.newCachedThreadPool());
	}

	private EndpointCallExecutableProvider[] executables() {
		return all(EndpointCallExecutableProvider.class).toArray(new EndpointCallExecutableProvider[0]);
	}

	private EndpointRequestExecutor endpointRequestExecutor() {
		return get(EndpointRequestExecutor.class, () -> null);
	}

	private RestifyContractReader contractReader() {
		return get(RestifyContractReader.class, () -> null);
	}

	private RestifyContractExpressionResolver expressionResolver() {
		return get(RestifyContractExpressionResolver.class, () -> null);
	}

	private HttpClientRequestFactory httpClientRequestFactory() {
		return get(HttpClientRequestFactory.class, () -> new JdkHttpClientRequestFactory());
	}

	private <T> T get(Class<? extends T> type, Supplier<T> supplier) {
		Instance<? extends T> instance = CDI.current().select(type);

		return instance.isUnsatisfied() ? supplier.get() : instance.get();
	}

	private <T> T get(Class<? extends T> type, Annotation qualifier, Supplier<T> supplier) {
		Instance<? extends T> instance = CDI.current().select(type, qualifier);

		return instance.isUnsatisfied() ? supplier.get() : instance.get();
	}

	private <T> Collection<T> all(Class<? extends T> type) {
		Instance<? extends T> instance = CDI.current().select(type);

		Collection<T> types = new ArrayList<>();

		instance.forEach(types::add);

		return types;
	}

	@SuppressWarnings("serial")
	private static final class RestifyExecutorLiteral extends AnnotationLiteral<RestifyExecutor> implements RestifyExecutor {
	}
}
