package com.restify.spring.autoconfigure;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.core.type.AnnotationMetadata;

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
import com.restify.spring.autoconfigure.RestifyAutoConfiguration.RestifyAutoConfigurationRegistrar;
import com.restify.spring.configure.BaseRestifyConfigurationRegistrar;
import com.restify.spring.configure.RestifyProxyFactoryBean;
import com.restify.spring.configure.RestifyableTypeScanner;

@Configuration
@Import(RestifyAutoConfigurationRegistrar.class)
@ConditionalOnMissingBean(RestifyProxyFactoryBean.class)
@AutoConfigureAfter(WebClientAutoConfiguration.class)
public class RestifyAutoConfiguration {

	@Configuration
	protected static class RestifySpringConfiguration {

		@ConditionalOnMissingBean
		@Bean
		public EndpointRequestExecutor endpointRequestExecutor(RestTemplateBuilder restTemplateBuilder) {
			return new RestOperationsEndpointRequestExecutor(restTemplateBuilder.build());
		}

		@ConditionalOnMissingBean
		@Bean
		public RestifyContractReader restifyContractReader(SpringDynamicParameterExpressionResolver expressionResolver) {
			return new SpringWebContractReader(expressionResolver);
		}

		@ConditionalOnMissingBean
		@Bean
		public SpringDynamicParameterExpressionResolver expressionResolver(ConfigurableBeanFactory beanFactory) {
			return new SpelDynamicParameterExpressionResolver(beanFactory);
		}

		@ConditionalOnMissingBean
		@Bean
		public HttpClientRequestFactory httpClientRequestFactory() {
			return new JdkHttpClientRequestFactory();
		}

		@ConditionalOnMissingBean
		@Bean
		public HttpHeadersEndpointCallExecutableFactory httpHeadersEndpointCallExecutableFactory() {
			return new HttpHeadersEndpointCallExecutableFactory();
		}

		@ConditionalOnMissingBean
		@Bean
		public ResponseEntityEndpointCallExecutableFactory<Object> responseEntityEndpointCallExecutableFactory() {
			return new ResponseEntityEndpointCallExecutableFactory<>();
		}
	}

	@Configuration
	protected static class RestifySpringAsyncRequestConfiguration {

		@Value("${restify.async.timeout:}")
		private Long asyncTimeout;

		@ConditionalOnMissingBean(name = "restifyAsyncTaskExecutor", value = AsyncListenableTaskExecutor.class)
		@Bean
		public AsyncListenableTaskExecutor restifyAsyncTaskExecutor() {
			return new SimpleAsyncTaskExecutor("RestifyAsyncTaskExecutor");
		}

		@ConditionalOnMissingBean(name = "restifyAsyncExecutorService", value = ExecutorService.class)
		@Bean
		public ExecutorService restifyAsyncExecutorService(@Qualifier("restifyAsyncTaskExecutor") TaskExecutor executor) {
			return new ExecutorServiceAdapter(executor);
		}

		@ConditionalOnMissingBean
		@Bean
		public AsyncResultEndpointCallExecutableFactory<Object, Object> asyncResultEndpointCallExecutableFactory() {
			return new AsyncResultEndpointCallExecutableFactory<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public DeferredResultEndpointCallExecutableFactory<Object, Object> deferredResultEndpointCallExecutableFactory(Environment environment, 
				@Qualifier("restifyAsyncTaskExecutor") Executor executor) {
			return new DeferredResultEndpointCallExecutableFactory<>(asyncTimeout, executor);
		}

		@ConditionalOnMissingBean
		@Bean
		public ListenableFutureEndpointCallExecutableFactory<Object, Object> listenableFutureEndpointCallExecutableFactory(@Qualifier("restifyAsyncTaskExecutor") AsyncListenableTaskExecutor executor) {
			return new ListenableFutureEndpointCallExecutableFactory<>(executor);
		}

		@ConditionalOnMissingBean
		@Bean
		public ListenableFutureTaskEndpointCallExecutableFactory<Object, Object> listenableFutureTaskEndpointCallExecutableFactory(@Qualifier("restifyAsyncTaskExecutor") AsyncListenableTaskExecutor executor) {
			return new ListenableFutureTaskEndpointCallExecutableFactory<>(executor);
		}

		@ConditionalOnMissingBean
		@Bean
		public WebAsyncTaskEndpointCallExecutableFactory<Object, Object> webAsyncTaskEndpointCallExecutableFactory(@Qualifier("restifyAsyncTaskExecutor") AsyncTaskExecutor executor) {
			return new WebAsyncTaskEndpointCallExecutableFactory<>(asyncTimeout, executor);
		}
	}

	protected static class RestifyAutoConfigurationRegistrar extends BaseRestifyConfigurationRegistrar {

		private RestifyableTypeScanner scanner = new RestifyableTypeScanner();

		@Override
		public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
			doScan(AutoConfigurationPackages.get(beanFactory), scanner, registry);
		}
	}
}
