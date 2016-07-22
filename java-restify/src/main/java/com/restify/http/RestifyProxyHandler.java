package com.restify.http;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Optional;

import com.restify.http.client.EndpointMethodExecutor;
import com.restify.http.metadata.EndpointMethod;
import com.restify.http.metadata.EndpointType;
import com.restify.http.metadata.reflection.JavaDefaultMethodExecutor;

public class RestifyProxyHandler implements InvocationHandler {

	private final EndpointType endpointType;
	private final EndpointMethodExecutor endpointMethodExecutor;
	private final JavaDefaultMethodExecutor javaDefaultMethodExecutor;

	public RestifyProxyHandler(EndpointType endpointType, EndpointMethodExecutor endpointMethodExecutor) {
		this.endpointType = endpointType;
		this.endpointMethodExecutor = endpointMethodExecutor;
		this.javaDefaultMethodExecutor = new JavaDefaultMethodExecutor();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Optional<EndpointMethod> endpointMethod = endpointType.find(method);

		return endpointMethod.isPresent() ? endpointMethodExecutor.execute(endpointMethod.get(), args)
				: method.isDefault() ?
					executeProxyMethod(method, proxy, args)
						: executeObjectMethod(method, args);
	}

	private Object executeProxyMethod(Method method, Object proxy, Object[] args) {
		try {
			return javaDefaultMethodExecutor.execute(method, proxy, args);
		} catch (Throwable e) {
			throw new RestifyProxyMethodException(e);
		}
	}

	private Object executeObjectMethod(Method method, Object[] args) {
		try {
			return method.invoke(endpointType, args);
		} catch (Exception e) {
			throw new RestifyProxyMethodException(e);
		}
	}
}
