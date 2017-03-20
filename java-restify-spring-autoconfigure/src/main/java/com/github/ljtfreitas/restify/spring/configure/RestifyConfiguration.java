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

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.apache.httpclient.ApacheHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.okhttp.OkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.response.DefaultEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.contract.metadata.RestifyContractExpressionResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.RestifyContractReader;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.AsyncResultEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.DeferredResultEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.HttpHeadersEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.ListenableFutureEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.ListenableFutureTaskEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.ResponseEntityEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.WebAsyncTaskEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.request.RestOperationsEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.spring.contract.SpringWebContractReader;
import com.github.ljtfreitas.restify.http.spring.contract.metadata.SpelDynamicParameterExpressionResolver;

@Configuration
class RestifyConfiguration {

	@Value("${restify.error.emptyOnNotFound:false}")
	private boolean emptyOnNotFound;

	@Bean
	public EndpointResponseErrorFallback endpointResponseErrorFallback() {
		return emptyOnNotFound ? DefaultEndpointResponseErrorFallback.emptyOnNotFound() : new DefaultEndpointResponseErrorFallback();
	}

	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "restify.http.client", havingValue = "jdk", matchIfMissing = true)
	@Bean
	public HttpClientRequestFactory jdkHttpClientRequestFactory() {
		return new JdkHttpClientRequestFactory();
	}

	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "restify.http.client", havingValue = "http-client")
	@Bean
	public HttpClientRequestFactory apacheHttpClientRequestFactory() {
		return new ApacheHttpClientRequestFactory();
	}

	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "restify.http.client", havingValue = "ok-http")
	@Bean
	public HttpClientRequestFactory okHttpClientRequestFactory() {
		return new OkHttpClientRequestFactory();
	}

	@Bean
	public HttpHeadersEndpointCallExecutableFactory httpHeadersEndpointCallExecutableFactory() {
		return new HttpHeadersEndpointCallExecutableFactory();
	}

	@Bean
	public ResponseEntityEndpointCallExecutableFactory<Object> responseEntityEndpointCallExecutableFactory() {
		return new ResponseEntityEndpointCallExecutableFactory<>();
	}

	@Configuration
	@ConditionalOnProperty(name = "restify.contract", havingValue = "spring-mvc", matchIfMissing = true)
	protected static class RestifySpringConfiguration {

		@Bean
		public EndpointRequestExecutor endpointRequestExecutor(ObjectProvider<RestOperations> restOperations,
				ObjectProvider<EndpointResponseErrorFallback> endpointResponseErrorFallback) {

			RestOperations rest = Optional.ofNullable(restOperations.getIfAvailable()).orElseGet(() -> new RestTemplate());
			EndpointResponseErrorFallback fallback = Optional.ofNullable(endpointResponseErrorFallback.getIfAvailable()).orElseGet(() -> new DefaultEndpointResponseErrorFallback());

			return new RestOperationsEndpointRequestExecutor(rest, fallback);
		}

		@Bean
		public RestifyContractReader restifyContractReader(RestifyContractExpressionResolver expressionResolver) {
			return new SpringWebContractReader(expressionResolver);
		}

		@Bean
		public RestifyContractExpressionResolver expressionResolver(ConfigurableBeanFactory beanFactory) {
			return new SpelDynamicParameterExpressionResolver(beanFactory);
		}

	}

	@Configuration
	protected static class RestifySpringAsyncRequestConfiguration {

		@Value("${restify.async.timeout:}")
		private Long asyncTimeout;

		@Bean
		public AsyncListenableTaskExecutor restifyAsyncTaskExecutor() {
			return new SimpleAsyncTaskExecutor("RestifyAsyncTaskExecutor");
		}

		@Bean
		public ExecutorService restifyAsyncExecutorService(@Qualifier("restifyAsyncTaskExecutor") TaskExecutor executor) {
			return new ExecutorServiceAdapter(executor);
		}

		@Bean
		public AsyncResultEndpointCallExecutableFactory<Object, Object> asyncResultEndpointCallExecutableFactory() {
			return new AsyncResultEndpointCallExecutableFactory<>();
		}

		@Bean
		public DeferredResultEndpointCallExecutableFactory<Object, Object> deferredResultEndpointCallExecutableFactory(Environment environment, 
				@Qualifier("restifyAsyncTaskExecutor") Executor executor) {
			return new DeferredResultEndpointCallExecutableFactory<>(asyncTimeout, executor);
		}

		@Bean
		public ListenableFutureEndpointCallExecutableFactory<Object, Object> listenableFutureEndpointCallExecutableFactory(@Qualifier("restifyAsyncTaskExecutor") AsyncListenableTaskExecutor executor) {
			return new ListenableFutureEndpointCallExecutableFactory<>(executor);
		}

		@Bean
		public ListenableFutureTaskEndpointCallExecutableFactory<Object, Object> listenableFutureTaskEndpointCallExecutableFactory(@Qualifier("restifyAsyncTaskExecutor") AsyncListenableTaskExecutor executor) {
			return new ListenableFutureTaskEndpointCallExecutableFactory<>(executor);
		}

		@Bean
		public WebAsyncTaskEndpointCallExecutableFactory<Object, Object> webAsyncTaskEndpointCallExecutableFactory(@Qualifier("restifyAsyncTaskExecutor") AsyncTaskExecutor executor) {
			return new WebAsyncTaskEndpointCallExecutableFactory<>(asyncTimeout, executor);
		}
	}
}
