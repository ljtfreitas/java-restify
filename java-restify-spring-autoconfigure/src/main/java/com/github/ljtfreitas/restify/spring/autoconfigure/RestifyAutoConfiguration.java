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
package com.github.ljtfreitas.restify.spring.autoconfigure;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
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
import com.github.ljtfreitas.restify.spring.autoconfigure.RestifyAutoConfiguration.RestifyAutoConfigurationRegistrar;
import com.github.ljtfreitas.restify.spring.configure.BaseRestifyConfigurationRegistrar;
import com.github.ljtfreitas.restify.spring.configure.RestifyProxyFactoryBean;
import com.github.ljtfreitas.restify.spring.configure.RestifyableTypeScanner;

@Configuration
@Import(RestifyAutoConfigurationRegistrar.class)
@ConditionalOnMissingBean(RestifyProxyFactoryBean.class)
@AutoConfigureAfter(WebClientAutoConfiguration.class)
public class RestifyAutoConfiguration {

	@Value("${restify.error.emptyOnNotFound:false}")
	private boolean emptyOnNotFound;

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

	@ConditionalOnMissingBean
	@Bean
	public EndpointResponseErrorFallback endpointResponseErrorFallback() {
		return emptyOnNotFound ? DefaultEndpointResponseErrorFallback.emptyOnNotFound() : new DefaultEndpointResponseErrorFallback();
	}

	@Configuration
	@ConditionalOnProperty(name = "restify.contract", havingValue = "spring-web", matchIfMissing = true)
	protected static class RestifySpringConfiguration {

		@Autowired(required = false)
		private RestTemplateBuilder restTemplateBuilder;

		@Autowired(required = false)
		private RestOperations restTemplate;

		@ConditionalOnMissingBean
		@Bean
		public EndpointRequestExecutor endpointRequestExecutor(EndpointResponseErrorFallback endpointResponseErrorFallback) {
			RestOperations restOperations = Optional.ofNullable(restTemplate)
					.orElseGet(() -> Optional.ofNullable(restTemplateBuilder)
						.map(builder -> builder.build())
							.orElseGet(() -> new RestTemplate()));

			return new RestOperationsEndpointRequestExecutor(restOperations, endpointResponseErrorFallback);
		}

		@ConditionalOnMissingBean
		@Bean
		public RestifyContractReader restifyContractReader(RestifyContractExpressionResolver expressionResolver) {
			return new SpringWebContractReader(expressionResolver);
		}

		@ConditionalOnMissingBean
		@Bean
		public RestifyContractExpressionResolver expressionResolver(ConfigurableBeanFactory beanFactory) {
			return new SpelDynamicParameterExpressionResolver(beanFactory);
		}
	}

	@Configuration
	protected static class RestifySpringAsyncRequestConfiguration {

		@Value("${restify.async.timeout:}")
		private Long asyncTimeout;

		@Conditional(RestifyAsyncTaskExecutorCondition.class)
		@Bean
		public AsyncListenableTaskExecutor restifyAsyncTaskExecutor() {
			return new SimpleAsyncTaskExecutor("RestifyAsyncTaskExecutor");
		}

		@Conditional(RestifyAsyncExecutorServiceCondition.class)
		@Bean
		public ExecutorService restifyAsyncExecutorService(@Qualifier("restifyAsyncTaskExecutor") AsyncTaskExecutor executor) {
			return new ExecutorServiceAdapter(executor);
		}

		@ConditionalOnMissingBean
		@Bean
		public AsyncResultEndpointCallExecutableFactory<Object, Object> asyncResultEndpointCallExecutableFactory() {
			return new AsyncResultEndpointCallExecutableFactory<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public DeferredResultEndpointCallExecutableFactory<Object, Object> deferredResultEndpointCallExecutableFactory(
				@Qualifier("restifyAsyncTaskExecutor") AsyncTaskExecutor executor) {
			return new DeferredResultEndpointCallExecutableFactory<>(asyncTimeout, executor);
		}

		@ConditionalOnMissingBean
		@Bean
		public ListenableFutureEndpointCallExecutableFactory<Object, Object> listenableFutureEndpointCallExecutableFactory(
				@Qualifier("restifyAsyncTaskExecutor") AsyncListenableTaskExecutor executor) {
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

		static class RestifyAsyncTaskExecutorCondition extends AllNestedConditions {

			RestifyAsyncTaskExecutorCondition() {
				super(ConfigurationPhase.REGISTER_BEAN);
			}

			@ConditionalOnMissingBean(name = "restifyAsyncTaskExecutor")
			static class OnRestifyAsyncTaskExecutorMissing {
			}

			@ConditionalOnMissingBean(value = AsyncListenableTaskExecutor.class, ignored = {
					SchedulingTaskExecutor.class, TaskScheduler.class, ScheduledExecutorService.class })
			static class OnAsyncListenableTaskExecutorMissing {
			}
		}

		static class RestifyAsyncExecutorServiceCondition extends AllNestedConditions {

			RestifyAsyncExecutorServiceCondition() {
				super(ConfigurationPhase.REGISTER_BEAN);
			}

			@ConditionalOnMissingBean(name = "restifyAsyncExecutorService")
			static class OnRestifyAsyncExecutorServiceMissing {
			}

			@ConditionalOnMissingBean(value = ExecutorService.class, ignored = { SchedulingTaskExecutor.class,
					TaskScheduler.class, ScheduledExecutorService.class })
			static class OnExecutorServiceMissing {
			}
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
