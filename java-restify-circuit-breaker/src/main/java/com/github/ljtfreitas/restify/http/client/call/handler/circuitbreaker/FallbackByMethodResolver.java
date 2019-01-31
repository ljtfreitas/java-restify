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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.github.ljtfreitas.restify.util.Try;

class FallbackByMethodResolver {

	private static final FallbackByMethodResolver SINGLE_INSTANCE = new FallbackByMethodResolver();

	private final Map<Method, Method> cache = new ConcurrentHashMap<>();

	Optional<Method> resolve(Object target, Method source, Object[] args, String name, Throwable throwable) {
		return Optional.ofNullable(cache.computeIfAbsent(source, m -> doResolve(target, m, args, name, throwable)));
	}

	private Method doResolve(Object target, Method source, Object[] args, String name, Throwable throwable) {
		String methodName =  (name == null ? source.getName() : name);

		return throwableAsFirstArgument(target, source, throwable.getClass(), methodName)
					.orElseGet(() -> throwableAsLastArgument(target, source, throwable.getClass(), methodName)
							.orElseGet(() -> signature(target, source, methodName)
									.orElse(null)));
	}

	private Optional<Method> throwableAsFirstArgument(Object target, Method javaMethod, Class<?> throwableType, String name) {
		if (throwableType == null || !Throwable.class.isAssignableFrom(throwableType)) return Optional.empty();

		int numberOfArguments = javaMethod.getParameterTypes().length;

		Class<?>[] argumentTypes = new Class[numberOfArguments + 1];
		argumentTypes[0] = throwableType;

		System.arraycopy(javaMethod.getParameterTypes(), 0, argumentTypes, 1, numberOfArguments);

		Optional<Method> method = signature(target, name, argumentTypes);
		return method.isPresent() ? method : throwableAsFirstArgument(target, javaMethod, throwableType.getSuperclass(), name);
	}

	private Optional<Method> throwableAsLastArgument(Object target, Method javaMethod, Class<?> throwableType, String name) {
		if (throwableType == null || !Throwable.class.isAssignableFrom(throwableType)) return Optional.empty();

		int numberOfArguments = javaMethod.getParameterTypes().length;

		Class<?>[] argumentTypes = new Class[numberOfArguments + 1];
		argumentTypes[numberOfArguments] = throwableType;

		System.arraycopy(javaMethod.getParameterTypes(), 0, argumentTypes, 0, numberOfArguments);

		Optional<Method> method = signature(target, name, argumentTypes);
		return method.isPresent() ? method : throwableAsLastArgument(target, javaMethod, throwableType.getSuperclass(), name);
	}

	private Optional<Method> signature(Object target, Method javaMethod, String name) {
		return signature(target, name, javaMethod.getParameterTypes());
	}

	private Optional<Method> signature(Object target, String name, Class<?>[] argTypes) {
		return Try.of(() -> target.getClass().getDeclaredMethod(name, argTypes))
				   .map(Optional::ofNullable)
				   .recover(NoSuchMethodException.class, e -> Try.success(Optional.empty()))
				   .get();
	}

	static FallbackByMethodResolver cached() {
		return SINGLE_INSTANCE;
	}
}
