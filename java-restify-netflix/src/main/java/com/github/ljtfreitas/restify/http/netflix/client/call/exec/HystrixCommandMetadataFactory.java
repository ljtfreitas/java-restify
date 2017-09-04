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
package com.github.ljtfreitas.restify.http.netflix.client.call.exec;

import java.util.Optional;

import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.netflix.client.request.circuitbreaker.CircuitBreakerProperty;
import com.github.ljtfreitas.restify.http.netflix.client.request.circuitbreaker.OnCircuitBreaker;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

class HystrixCommandMetadataFactory {

	private final EndpointMethod endpointMethod;
	private final Optional<OnCircuitBreaker> onCircuitBreaker;

	public HystrixCommandMetadataFactory(EndpointMethod endpointMethod) {
		this.endpointMethod = endpointMethod;
		this.onCircuitBreaker = endpointMethod.metadata().get(OnCircuitBreaker.class);
	}

	public HystrixCommand.Setter create() {
		HystrixCommand.Setter setter = HystrixCommand.Setter
				.withGroupKey(groupKey())
					.andCommandKey(commandKey())
						.andThreadPoolKey(threadPoolKey())
							.andCommandPropertiesDefaults(commandProperties())
								.andThreadPoolPropertiesDefaults(threadPoolProperties());

		return setter;
	}

	private HystrixCommandGroupKey groupKey() {
		String groupKey = onCircuitBreaker.map(a -> a.groupKey())
				.filter(g -> g != null && !"".equals(g))
					.orElseGet(() -> endpointMethod.javaMethod().getDeclaringClass().getSimpleName());

		return HystrixCommandGroupKey.Factory.asKey(groupKey);
	}

	private HystrixCommandKey commandKey() {
		String commandKey = onCircuitBreaker.map(a -> a.commandKey())
				.filter(g -> g != null && !"".equals(g))
					.orElseGet(() -> endpointMethod.javaMethod().getName());

		return HystrixCommandKey.Factory.asKey(commandKey);
	}

	private HystrixThreadPoolKey threadPoolKey() {
		String threadPoolKey = onCircuitBreaker.map(a -> a.threadPoolKey())
				.filter(g -> g != null && !"".equals(g))
					.orElse(null);

		return Optional.ofNullable(threadPoolKey)
					.map(k -> HystrixThreadPoolKey.Factory.asKey(k)).orElse(null);
	}

	private HystrixCommandProperties.Setter commandProperties() {
		CircuitBreakerProperty[] properties = onCircuitBreaker.map(a -> a.properties())
				.orElseGet(() -> new CircuitBreakerProperty[0]);

		HystrixCommandProperties.Setter hystrixCommandProperties = HystrixCommandProperties.defaultSetter();

		HystrixCircuitBreakerCommandPropertiesSetter propertiesSetter = new HystrixCircuitBreakerCommandPropertiesSetter(properties);
		propertiesSetter.applyTo(hystrixCommandProperties);

		return hystrixCommandProperties;
	}

	private HystrixThreadPoolProperties.Setter threadPoolProperties() {
		CircuitBreakerProperty[] properties = onCircuitBreaker.map(a -> a.properties())
				.orElseGet(() -> new CircuitBreakerProperty[0]);

		HystrixThreadPoolProperties.Setter hystrixThreadPoolProperties = HystrixThreadPoolProperties.defaultSetter();

		HystrixCircuitBreakerThreadPoolPropertiesSetter propertiesSetter = new HystrixCircuitBreakerThreadPoolPropertiesSetter(properties);
		propertiesSetter.applyTo(hystrixThreadPoolProperties);

		return hystrixThreadPoolProperties;
	}
}
