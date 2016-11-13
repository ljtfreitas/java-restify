package com.restify.spring.configure;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.web.client.RestTemplate;

import com.restify.http.client.request.EndpointRequestExecutor;
import com.restify.http.client.request.HttpClientRequestFactory;
import com.restify.http.client.request.jdk.JdkHttpClientRequestFactory;
import com.restify.http.contract.metadata.RestifyContractReader;
import com.restify.http.spring.client.call.exec.AsyncResultEndpointCallExecutableFactory;
import com.restify.http.spring.client.call.exec.DeferredResultEndpointCallExecutableFactory;
import com.restify.http.spring.client.call.exec.HttpHeadersEndpointCallExecutableFactory;
import com.restify.http.spring.client.call.exec.ListenableFutureEndpointCallExecutableFactory;
import com.restify.http.spring.client.call.exec.ListenableFutureTaskEndpointCallExecutableFactory;
import com.restify.http.spring.client.call.exec.ResponseEntityEndpointCallExecutableFactory;
import com.restify.http.spring.client.call.exec.WebAsyncTaskEndpointCallExecutableFactory;
import com.restify.http.spring.client.request.RestOperationsEndpointRequestExecutor;
import com.restify.http.spring.contract.SpringWebContractReader;
import com.restify.http.spring.contract.metadata.SpelDynamicParameterExpressionResolver;
import com.restify.http.spring.contract.metadata.SpringDynamicParameterExpressionResolver;

@Configuration
class RestifyConfiguration {

	@Configuration
	protected static class RestifySpringConfiguration {
		@Bean
		public EndpointRequestExecutor endpointRequestExecutor(ObjectProvider<RestTemplate> restTemplate) {
			return new RestOperationsEndpointRequestExecutor(
					Optional.ofNullable(restTemplate.getIfAvailable()).orElseGet(() -> new RestTemplate()));
		}

		@Bean
		public RestifyContractReader restifyContractReader(SpringDynamicParameterExpressionResolver expressionResolver) {
			return new SpringWebContractReader(expressionResolver);
		}

		@Bean
		public SpringDynamicParameterExpressionResolver expressionResolver(ConfigurableBeanFactory beanFactory) {
			return new SpelDynamicParameterExpressionResolver(beanFactory);
		}

		@Bean
		public HttpClientRequestFactory httpClientRequestFactory() {
			return new JdkHttpClientRequestFactory();
		}

		@Bean
		public HttpHeadersEndpointCallExecutableFactory httpHeadersEndpointCallExecutableFactory() {
			return new HttpHeadersEndpointCallExecutableFactory();
		}

		@Bean
		public ResponseEntityEndpointCallExecutableFactory<Object> responseEntityEndpointCallExecutableFactory() {
			return new ResponseEntityEndpointCallExecutableFactory<>();
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
		public AsyncResultEndpointCallExecutableFactory<Object> asyncResultEndpointCallExecutableFactory() {
			return new AsyncResultEndpointCallExecutableFactory<>();
		}

		@Bean
		public DeferredResultEndpointCallExecutableFactory<Object> deferredResultEndpointCallExecutableFactory(Environment environment, 
				@Qualifier("restifyAsyncTaskExecutor") Executor executor) {
			return new DeferredResultEndpointCallExecutableFactory<>(asyncTimeout, executor);
		}

		@Bean
		public ListenableFutureEndpointCallExecutableFactory<Object> listenableFutureEndpointCallExecutableFactory(@Qualifier("restifyAsyncTaskExecutor") AsyncListenableTaskExecutor executor) {
			return new ListenableFutureEndpointCallExecutableFactory<>(executor);
		}

		@Bean
		public ListenableFutureTaskEndpointCallExecutableFactory<Object> listenableFutureTaskEndpointCallExecutableFactory(@Qualifier("restifyAsyncTaskExecutor") AsyncListenableTaskExecutor executor) {
			return new ListenableFutureTaskEndpointCallExecutableFactory<>(executor);
		}

		@Bean
		public WebAsyncTaskEndpointCallExecutableFactory<Object> webAsyncTaskEndpointCallExecutableFactory(@Qualifier("restifyAsyncTaskExecutor") AsyncTaskExecutor executor) {
			return new WebAsyncTaskEndpointCallExecutableFactory<>(asyncTimeout, executor);
		}
	}
}
