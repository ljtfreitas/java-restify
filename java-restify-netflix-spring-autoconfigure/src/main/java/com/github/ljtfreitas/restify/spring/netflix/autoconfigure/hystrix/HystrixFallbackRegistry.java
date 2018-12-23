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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;

import com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix.WithFallback;
import com.github.ljtfreitas.restify.util.Tryable;

class HystrixFallbackRegistry {

	static final String QUALIFIER_NAME = "fallback";

	private final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();

	private final BeanFactory beanFactory;

	public HystrixFallbackRegistry(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	Optional<Object> get(Class<?> classType) {
		Object fallback = (cache.containsKey(classType) ? cache.get(classType) : search(classType));
		return Optional.ofNullable(fallback);
	}

	private Object search(Class<?> classType) {
		Object bean = searchWithQualifier(classType)
				.orElseGet(() -> searchWithType(classType.getAnnotation(WithFallback.class))
						.orElseGet(null));

		if (bean != null) cache.put(classType, bean);

		return bean;
	}

	private Optional<Object> searchWithType(WithFallback withFallback) {
		return Optional.ofNullable(withFallback)
			.map(w -> Tryable.or(() -> beanFactory.getBean(w.value()), null));
	}

	private Optional<Object> searchWithQualifier(Class<?> classType) {
		return Optional.ofNullable(Tryable.or(() -> BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, classType, QUALIFIER_NAME), null));
	}
}
