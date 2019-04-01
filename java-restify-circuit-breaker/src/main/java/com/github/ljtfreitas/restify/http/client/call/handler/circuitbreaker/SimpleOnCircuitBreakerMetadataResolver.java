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
package com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;

public class SimpleOnCircuitBreakerMetadataResolver implements OnCircuitBreakerMetadataResolver {

	@Override
	public OnCircuitBreakerMetadata resolve(EndpointMethod endpointMethod) {
		return endpointMethod.metadata().get(OnCircuitBreaker.class)
				.map(this::doResolve)
					.orElseGet(EmptyOnCircuitBreakerMetadata::new);
	}

	private OnCircuitBreakerMetadata doResolve(OnCircuitBreaker onCircuitBreaker) {
		Predicate<String> notEmpty = s -> !s.isEmpty();

		String groupKey = Optional.ofNullable(onCircuitBreaker.groupKey()).filter(notEmpty).orElse(null);
		String commandKey = Optional.ofNullable(onCircuitBreaker.commandKey()).filter(notEmpty).orElse(null);
		String threadPoolKey = Optional.ofNullable(onCircuitBreaker.threadPoolKey()).filter(notEmpty).orElse(null);
		Map<String, String> properties = Arrays.stream(onCircuitBreaker.properties())
				.collect(Collectors.toMap(CircuitBreakerProperty::name, CircuitBreakerProperty::value));

		return new SimpleOnCircuitBreakerMetadata(groupKey, commandKey, threadPoolKey, properties);
	}

	private class SimpleOnCircuitBreakerMetadata implements OnCircuitBreakerMetadata {

		private final String groupKey;
		private final String commandKey;
		private final String threadPoolKey;
		private final Map<String, String> properties;

		private SimpleOnCircuitBreakerMetadata(String groupKey, String commandKey, String threadPoolKey,
				Map<String, String> properties) {
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
