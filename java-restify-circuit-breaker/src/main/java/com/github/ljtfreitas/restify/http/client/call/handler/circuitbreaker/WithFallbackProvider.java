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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import com.github.ljtfreitas.restify.util.Try;

public class WithFallbackProvider implements FallbackProvider {

	private final WithFallbackCache cache = WithFallbackCache.instance();

	@Override
	public Fallback provides(Method javaMethod) {
		WithFallback withFallback = onMethod(javaMethod).orElseGet(() -> onType(javaMethod.getDeclaringClass()));
		return withFallback == null ? Fallback.empty() : Fallback.of(using(withFallback.value()), withFallback.method());
	}

	private Optional<WithFallback> onMethod(Method javaMethod) {
		 return Optional.ofNullable(javaMethod.getDeclaredAnnotation(WithFallback.class));
	}

	private WithFallback onType(Class<?> type) {
		return type.getDeclaredAnnotation(WithFallback.class);
	}

	private Object using(Class<?> type) {
		return cache.get(type).orElseGet(() -> Try.of(() -> instantiate(type)).error(IllegalArgumentException::new).get());
	}

	private Object instantiate(Class<?> type) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		return constructor.newInstance();
	}
}
