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
package com.github.ljtfreitas.restify.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class JavaDefaultMethodExecutor {

	private static final Map<Method, MethodHandle> cache = new ConcurrentHashMap<>();

	public static Object execute(Method method, Object target, Object[] args) throws Throwable {
		MethodHandle handle = cache.compute(method,
				(m, h) -> Optional.ofNullable(h).orElseGet(() -> bind(method, target)));

		return handle.invokeWithArguments(args);
	}

	private static MethodHandle bind(Method method, Object target) {
		try {
			Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class, int.class);
			constructor.setAccessible(true);

			return constructor.newInstance(method.getDeclaringClass(), Lookup.PRIVATE)
					.unreflectSpecial(method, method.getDeclaringClass())
						.bindTo(target);

		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InstantiationException | IllegalArgumentException
				| InvocationTargetException e) {

			throw new MethodExecutionException("Error on create MethodHandle to method [" + method + "], "
					+ "and target type [" + target.getClass() + "]", e);
		}
	}
}
