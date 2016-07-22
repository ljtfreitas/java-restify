package com.restify.http.metadata.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.restify.http.RestifyProxyMethodException;

public class JavaDefaultMethodExecutor {

	private final Map<Method, MethodHandle> cache = new ConcurrentHashMap<>();

	public Object execute(Method method, Object target, Object[] args) throws Throwable {
		MethodHandle handle = cache.compute(method,
				(m, h) -> Optional.ofNullable(h).orElseGet(() -> bind(method, target)));

		return handle.invokeWithArguments(args);
	}

	private MethodHandle bind(Method method, Object target) {
		try {
			Constructor<MethodHandles.Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class, int.class);

			constructor.setAccessible(true);

			return constructor.newInstance(method.getDeclaringClass(), Lookup.PRIVATE)
					.unreflectSpecial(method, method.getDeclaringClass())
						.bindTo(target);

		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InstantiationException | IllegalArgumentException
				| InvocationTargetException e) {

			throw new RestifyProxyMethodException(e);
		}
	}
}
