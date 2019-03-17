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
package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadata;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadataResolver;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.SimpleOnCircuitBreakerMetadataResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

public class HystrixCommandMetadataFactory {

	private final OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver;

	public HystrixCommandMetadataFactory() {
		this.onCircuitBreakerMetadataResolver = new SimpleOnCircuitBreakerMetadataResolver();
	}

	public HystrixCommandMetadataFactory(OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		this.onCircuitBreakerMetadataResolver = onCircuitBreakerMetadataResolver;
	}

	public HystrixCommandMetadata create(EndpointMethod endpointMethod) {
		return HystrixCommandMetadataCache.instance()
				.compute(endpointMethod, () -> doCreate(endpointMethod));
	}

	private HystrixCommandMetadata doCreate(EndpointMethod endpointMethod) {
		OnCircuitBreakerMetadata onCircuitBreaker = onCircuitBreakerMetadataResolver.resolve(endpointMethod);

		return new HystrixCommandMetadata.HystrixCommandMetadataBuilder()
				.withGroupKey(groupKey(endpointMethod, onCircuitBreaker))
					.andCommandKey(commandKey(endpointMethod, onCircuitBreaker))
						.andThreadPoolKey(threadPoolKey(onCircuitBreaker))
							.andCommandPropertiesDefaults(commandProperties(onCircuitBreaker))
								.andThreadPoolPropertiesDefaults(threadPoolProperties(onCircuitBreaker))
									.build();
	}

	private HystrixCommandGroupKey groupKey(EndpointMethod endpointMethod, OnCircuitBreakerMetadata onCircuitBreaker) {
		String groupKey = onCircuitBreaker.groupKey()
					.orElseGet(() -> endpointMethod.javaMethod().getDeclaringClass().getSimpleName());

		return HystrixCommandGroupKey.Factory.asKey(groupKey);
	}

	private HystrixCommandKey commandKey(EndpointMethod endpointMethod, OnCircuitBreakerMetadata onCircuitBreaker) {
		String commandKey = onCircuitBreaker.commandKey()
					.orElseGet(() -> endpointMethod.javaMethod().getName());

		return HystrixCommandKey.Factory.asKey(commandKey);
	}

	private HystrixThreadPoolKey threadPoolKey(OnCircuitBreakerMetadata onCircuitBreaker) {
		String threadPoolKey = onCircuitBreaker.threadPoolKey()
					.orElse(null);

		return Optional.ofNullable(threadPoolKey)
					.map(k -> HystrixThreadPoolKey.Factory.asKey(k)).orElse(null);
	}

	private HystrixCommandProperties.Setter commandProperties(OnCircuitBreakerMetadata onCircuitBreaker) {
		HystrixCommandProperties.Setter hystrixCommandProperties = HystrixCommandProperties.defaultSetter();

		HystrixCommandPropertiesWriter propertiesSetter = new HystrixCommandPropertiesWriter(onCircuitBreaker.properties());
		propertiesSetter.applyTo(hystrixCommandProperties);

		return hystrixCommandProperties;
	}

	private HystrixThreadPoolProperties.Setter threadPoolProperties(OnCircuitBreakerMetadata onCircuitBreaker) {
		HystrixThreadPoolProperties.Setter hystrixThreadPoolProperties = HystrixThreadPoolProperties.defaultSetter();

		HystrixThreadPoolPropertyWriter propertiesSetter = new HystrixThreadPoolPropertyWriter(onCircuitBreaker.properties());
		propertiesSetter.applyTo(hystrixThreadPoolProperties);

		return hystrixThreadPoolProperties;
	}
}
