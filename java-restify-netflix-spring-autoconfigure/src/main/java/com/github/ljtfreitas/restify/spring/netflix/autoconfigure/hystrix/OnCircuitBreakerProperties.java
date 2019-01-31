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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

class OnCircuitBreakerProperties {

	private final Binder binder;

	OnCircuitBreakerProperties(Environment environment) {
		this.binder = Binder.get(environment);
	}

	OnCircuitBreakerOfBean of(String bean) {
		return new OnCircuitBreakerOfBean(bean);
	}
	
	class OnCircuitBreakerOfBean {
		
		private final String bean;

		public OnCircuitBreakerOfBean(String bean) {
			this.bean = bean;
		}

		Optional<String> groupKey() {
			return property("group-key").resolveAsString();
		}

		Optional<String> commandKey(String groupKey) {
			String resolved = property("command-key").resolveAsString()
					.orElseGet(() -> property("[" +  groupKey + "].command-key")
							.resolveAsString()
								.orElse(null));

			return Optional.ofNullable(resolved);
		}


		Optional<String> threadPoolKey(String groupKey) {
			String resolved = property("thread-pool-key").resolveAsString()
				.orElseGet(() -> property("[" +  groupKey + "].thread-pool-key")
					.resolveAsString()
						.orElse(null));

			return Optional.ofNullable(resolved);
		}

		Map<String, String> properties(String groupKey, String commandKey) {
			Map<String, String> all = new LinkedHashMap<>();
			
			all.putAll(property("properties").resolveAsMap());
			all.putAll(property("[" + groupKey + "].properties").resolveAsMap());
			all.putAll(property("[" + groupKey + "].[" + commandKey + "].properties").resolveAsMap());

			return all;
		}

		private Property property(String property) {
			return new Property("restify." + bean + ".circuit-breaker." + property);
		}
	}
	
	private class Property {
		
		private final String property;

		private Property(String property) {
			this.property = property;
		}

		private Optional<String> resolveAsString() {
			return binder.bind(property, String.class)
				.map(Optional::ofNullable).orElseGet(Optional::empty);
		}

		private Map<String, String> resolveAsMap() {
			return binder.bind(property, Bindable.mapOf(String.class, String.class))
				.orElseGet(Collections::emptyMap);
		}
	}
}
