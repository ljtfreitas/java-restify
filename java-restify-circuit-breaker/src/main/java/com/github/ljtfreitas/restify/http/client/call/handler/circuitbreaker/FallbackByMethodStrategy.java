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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.github.ljtfreitas.restify.util.Try;

class FallbackByMethodStrategy implements FallbackStrategy {

	private final Object target;
	private final String method;

	FallbackByMethodStrategy(Object instance, String method) {
		this.target = instance;
		this.method = method;
	}

	@Override
	public FallbackResult<Object> execute(Method javaMethod, Object[] args, Throwable throwable) {
		Method fallbackMethod = FallbackByMethodResolver.cached()
									.resolve(target, javaMethod, args, method, throwable)
										.orElseThrow(() -> new IllegalArgumentException(target.getClass() + " can't supply a fallback to method " + javaMethod));

		return Try.run(() -> fallbackMethod.setAccessible(true))
				   .map(signal -> invoke(fallbackMethod, args, throwable))
				   .map(DefaultFallbackResult::new)
				   .get();
	}

	private Object invoke(Method fallbackMethod, Object[] args, Throwable throwable)
			throws IllegalAccessException, InvocationTargetException {
		return fallbackMethod.invoke(target, argsTo(fallbackMethod.getParameterTypes(), throwable, args == null ? new Object[0] : args));
	}

	private Object[] argsTo(Class<?>[] parameterTypes, Throwable throwable, Object[] args) {
		if (parameterTypes.length != 0) {
			if (parameterTypes[0].isAssignableFrom(throwable.getClass())) {
				return argsToExceptionAsFirstArgument(throwable, args);

			} else if (parameterTypes[parameterTypes.length - 1].isAssignableFrom(throwable.getClass())) {
				return argsToExceptionAsLastArgument(throwable, args);

			}
		}

		return args;
	}

	private Object[] argsToExceptionAsFirstArgument(Throwable throwable, Object[] args) {
		Object[] newArguments = new Object[args.length + 1];
		newArguments[0] = throwable;

		System.arraycopy(args, 0, newArguments, 1, args.length);

		return newArguments;
	}

	private Object[] argsToExceptionAsLastArgument(Throwable throwable, Object[] args) {
		Object[] newArguments = new Object[args.length + 1];
		newArguments[newArguments.length - 1] = throwable;

		System.arraycopy(args, 0, newArguments, 0, args.length);

		return newArguments;
	}
}