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
package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreaker;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadataResolver;
import com.github.ljtfreitas.restify.http.client.call.handler.reactor.HystrixFluxEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.reactor.HystrixMonoEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix.HystrixCommandEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix.HystrixEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix.HystrixFutureEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix.HystrixObservableCommandEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix.HystrixObservableEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.spring.autoconfigure.RestifyAutoConfiguration;

@Configuration
@ConditionalOnClass(OnCircuitBreaker.class)
@AutoConfigureBefore(RestifyAutoConfiguration.class)
public class RestifyHystrixAutoConfiguration {

	private static final int HYSTRIX_HANDLER_BEANS_PRIORITY_ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

	@ConditionalOnMissingBean
	@Bean
	@Order(HYSTRIX_HANDLER_BEANS_PRIORITY_ORDER + 1)
	public HystrixEndpointCallHandlerAdapter<Object, Object> hystrixEndpointCallHandlerAdapter() {
		return new HystrixEndpointCallHandlerAdapter<>();
	}

	@ConditionalOnMissingBean
	@Bean
	@Order(HYSTRIX_HANDLER_BEANS_PRIORITY_ORDER + 1)
	public HystrixFutureEndpointCallHandlerAdapter<Object, Object> hystrixFutureEndpointCallHandlerAdapter(
			HystrixFallbackRegistry fallbackObjectFactory, OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		return new HystrixFutureEndpointCallHandlerAdapter<>();
	}

	@ConditionalOnMissingBean
	@Bean
	@Order(HYSTRIX_HANDLER_BEANS_PRIORITY_ORDER + 1)
	public HystrixObservableEndpointCallHandlerAdapter<Object, Object> hystrixObservableEndpointCallHandlerAdapter() {
		return new HystrixObservableEndpointCallHandlerAdapter<>();
	}

	@ConditionalOnMissingBean
	@Bean
	@Order(HYSTRIX_HANDLER_BEANS_PRIORITY_ORDER + 10)
	public HystrixCommandEndpointCallHandlerAdapter<Object, Object> hystrixCommandFallbackEndpointCallHandlerAdapter(
			HystrixFallbackProvider hystrixFallbackProvider, OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		return new HystrixCommandEndpointCallHandlerAdapter<>(hystrixFallbackProvider, onCircuitBreakerMetadataResolver);
	}

	@ConditionalOnMissingBean
	@Bean
	@Order(HYSTRIX_HANDLER_BEANS_PRIORITY_ORDER + 10)
	public HystrixObservableCommandEndpointCallHandlerAdapter<Object, Object> hystrixObservableCommandFallbackEndpointCallHandlerAdapter(
			HystrixFallbackProvider hystrixFallbackProvider, OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		return new HystrixObservableCommandEndpointCallHandlerAdapter<>(hystrixFallbackProvider, onCircuitBreakerMetadataResolver);
	}

	@Configuration
	protected static class HystrixFallbackConfiguration implements BeanFactoryAware {

		private BeanFactory beanFactory;

		@Bean
		@ConditionalOnMissingBean
		public HystrixFallbackProvider hystrixFallbackProvider() {
			return new HystrixFallbackProvider(hystrixFallbackRegistry());
		}

		@Bean
		public HystrixFallbackRegistry hystrixFallbackRegistry() {
			return new HystrixFallbackRegistry(beanFactory);
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}
	}

	@Configuration
	protected static class HystrixOnCircuitBreakerMetadataResolverConfiguration
		implements BeanFactoryAware, ApplicationContextAware, EnvironmentAware {

		private BeanFactory beanFactory;
		private Environment environment;
		private ApplicationContext applicationContext;

		@Bean
		@ConditionalOnMissingBean
		public OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver() {
			return new SpelOnCircuitBreakerMetadataResolver((ConfigurableBeanFactory) beanFactory, applicationContext, onCircuitBreakerProperties());
		}

		@Bean
		public OnCircuitBreakerProperties onCircuitBreakerProperties() {
			return new OnCircuitBreakerProperties(environment);
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}

		@Override
		public void setEnvironment(Environment environment) {
			this.environment = environment;
		}

		@Override
		public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
			this.applicationContext = applicationContext;
		}
	}

	@Configuration
	@ConditionalOnClass(value = {HystrixFluxEndpointCallHandlerAdapter.class, HystrixMonoEndpointCallHandlerAdapter.class})
	protected static class ReactorHystrixConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@Order(HYSTRIX_HANDLER_BEANS_PRIORITY_ORDER + 2)
		public 	HystrixFluxEndpointCallHandlerAdapter<Object, Object> hystrixFluxEndpointCallHandlerAdapter(
				HystrixFallbackProvider hystrixFallbackProvider, OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
			return new HystrixFluxEndpointCallHandlerAdapter<>(hystrixFallbackProvider, onCircuitBreakerMetadataResolver);
		}

		@Bean
		@ConditionalOnMissingBean
		@Order(HYSTRIX_HANDLER_BEANS_PRIORITY_ORDER + 2)
		public 	HystrixMonoEndpointCallHandlerAdapter<Object, Object> hystrixMonoEndpointCallHandlerAdapter(
				HystrixFallbackProvider hystrixFallbackProvider, OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
			return new HystrixMonoEndpointCallHandlerAdapter<>(hystrixFallbackProvider, onCircuitBreakerMetadataResolver);
		}
	}
}
