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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;

import com.github.ljtfreitas.restify.util.Tryable;

class HystrixFallbackRegistry {

	static final String QUALIFIER_NAME = "fallback";

	private static final Map<Class<?>, Object> cache = new HashMap<>();

	private final BeanFactory beanFactory;

	public HystrixFallbackRegistry(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(Class<? extends T> classType) {
		T fallback = (T) (cache.containsKey(classType) ? cache.get(classType) : search(classType));
		return Optional.ofNullable(fallback);
	}

	private Object search(Class<?> classType) {
		Object bean = doSearch(classType);

		cache.put(classType, bean);

		return bean;
	}

	private Object doSearch(Class<?> classType) {
		return Tryable.or(() -> BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, classType, QUALIFIER_NAME), null);
	}
}
