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

import java.lang.reflect.Method;

class DefaultFallback implements Fallback {

	private final Object fallback;

	public DefaultFallback(Object fallback) {
		this.fallback = fallback;
	}

	@Override
	public FallbackStrategy strategy(Class<?> declaringClass) {
		return fallback == null ? new FallbackNotImplementedStrategy() :
			declaringClass.isAssignableFrom(fallback.getClass()) ?
				new FallbackOfSameType() : new FallbackByMethod();
	}

	private class FallbackOfSameType implements FallbackStrategy {

		@Override
		public FallbackResult<Object> execute(Method javaMethod, Object[] args) throws Exception {
			return new DefaultFallbackResult(javaMethod.invoke(fallback, args));
		}
	}

	private class FallbackByMethod implements FallbackStrategy {

		@Override
		public FallbackResult<Object> execute(Method javaMethod, Object[] args) throws Exception {
			Method fallbackMethod = fallback.getClass()
					.getMethod(javaMethod.getName(), javaMethod.getParameterTypes());
			return new DefaultFallbackResult(fallbackMethod.invoke(fallback, args));
		}
	}

	static DefaultFallback empty() {
		return new DefaultFallback(null);
	}
}
