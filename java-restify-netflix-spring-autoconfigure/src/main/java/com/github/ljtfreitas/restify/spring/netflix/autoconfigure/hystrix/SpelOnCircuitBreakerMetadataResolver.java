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

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.context.ApplicationContext;

import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadata;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadataResolver;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.SimpleOnCircuitBreakerMetadataResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.util.Try;

public class SpelOnCircuitBreakerMetadataResolver implements OnCircuitBreakerMetadataResolver {

	private final ConfigurableBeanFactory beanFactory;
	private final ApplicationContext applicationContext;
	private final OnCircuitBreakerProperties onCircuitBreakerProperties;
	private final OnCircuitBreakerMetadataResolver delegate;

	public SpelOnCircuitBreakerMetadataResolver(ConfigurableBeanFactory beanFactory, ApplicationContext applicationContext, OnCircuitBreakerProperties onCircuitBreakerProperties) {
		this.beanFactory = beanFactory;
		this.applicationContext = applicationContext;
		this.onCircuitBreakerProperties = onCircuitBreakerProperties;
		this.delegate = new SimpleOnCircuitBreakerMetadataResolver();
	}

	@Override
	public OnCircuitBreakerMetadata resolve(EndpointMethod endpointMethod) {
		OnCircuitBreakerMetadata onCircuitBreaker = delegate.resolve(endpointMethod);

		Class<?> type = endpointMethod.javaMethod().getDeclaringClass();

		Optional<String> beanName = beanNameTo(type);

		String groupKey = onCircuitBreaker.groupKey()
			.map(value -> resolve(value).orElse(value))
				.orElseGet(() -> beanName.flatMap(name -> onCircuitBreakerProperties.of(name).groupKey())
					.orElseGet(endpointMethod.javaMethod().getDeclaringClass()::getSimpleName));

		String commandKey = onCircuitBreaker.commandKey()
			.map(value -> resolve(value).orElse(value))
				.orElseGet(() -> beanName.flatMap(name -> onCircuitBreakerProperties.of(name).commandKey(groupKey))
					.orElseGet(endpointMethod.javaMethod()::getName));

		String threadPoolKey = onCircuitBreaker.threadPoolKey()
			.map(value -> resolve(value).orElse(value))
				.orElseGet(() -> beanName.flatMap(name -> onCircuitBreakerProperties.of(name).threadPoolKey(groupKey))
					.orElse(null));

		Map<String, String> properties = onCircuitBreaker.properties().entrySet().stream()
			.collect(Collectors.toMap(Entry::getKey, entry -> resolve(entry.getValue()).orElse(entry.getValue())));

		properties.putAll(beanName.map(name -> onCircuitBreakerProperties.of(name).properties(groupKey, commandKey))
				.orElseGet(Collections::emptyMap));

		return new SpelOnCircuitBreakerMetada(groupKey, commandKey, threadPoolKey, properties);
	}

	private Optional<String> beanNameTo(Class<?> type) {
		return Try.of(() -> applicationContext.getAutowireCapableBeanFactory().resolveNamedBean(type))
					.map(NamedBeanHolder::getBeanName)
					.recover(NoSuchBeanDefinitionException.class, e -> Try.success(null))
					.map(Optional::ofNullable)
					.get();
	}

	private Optional<String> resolve(String expression) {
		Optional<String> resolved = Optional.ofNullable(beanFactory.resolveEmbeddedValue(expression));
		return resolved.filter(value -> !expression.equals(value));
	}

	private class SpelOnCircuitBreakerMetada implements OnCircuitBreakerMetadata {

		private final String groupKey;
		private final String commandKey;
		private final String threadPoolKey;
		private final Map<String, String> properties;

		private SpelOnCircuitBreakerMetada(String groupKey, String commandKey, String threadPoolKey, Map<String, String> properties) {
			this.groupKey = groupKey;
			this.commandKey = commandKey;
			this.threadPoolKey = threadPoolKey;
			this.properties = properties;
		}

		@Override
		public Optional<String> groupKey() {
			return Optional.ofNullable(groupKey);
		}

		@Override
		public Optional<String> commandKey() {
			return Optional.ofNullable(commandKey);
		}

		@Override
		public Optional<String> threadPoolKey() {
			return Optional.ofNullable(threadPoolKey);
		}

		@Override
		public Map<String, String> properties() {
			return properties;
		}
	}

}
