package com.restify.http;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Optional;

import com.restify.http.client.EndpointMethodExecutor;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.EndpointType;
import com.restify.http.contract.metadata.reflection.JavaDefaultMethodExecutor;

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
						: executeObjectMethod(method, proxy, args);
	}

	private Object executeProxyMethod(Method method, Object proxy, Object[] args) {
		try {
			return javaDefaultMethodExecutor.execute(method, proxy, args);
		} catch (Throwable e) {
			throw new RestifyProxyMethodException("Error on execution method [" + method + "], "
					+ "on proxy object type [" + proxy.getClass() + "], with method args " + Arrays.toString(args), e);
		}
	}

	private Object executeObjectMethod(Method method, Object proxy, Object[] args) {
		try {
			return method.invoke(this, args);
		} catch (Exception e) {
			throw new RestifyProxyMethodException("Error on execution Object method [" + method + "], "
					+ "with method args " + Arrays.toString(args) + ". Method " + method + " is a Object method?", e);
		}
	}

	@Override
	public String toString() {
		return endpointType.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (endpointType.javaType().isAssignableFrom(obj.getClass())
				&& Proxy.isProxyClass(obj.getClass())) {

			RestifyProxyHandler that = (RestifyProxyHandler) Proxy.getInvocationHandler(obj);
			return endpointType.equals(that.endpointType);

		} else if (endpointType.getClass().isAssignableFrom(obj.getClass())) {
			return endpointType.equals(obj);

		} else return false;
	}

	@Override
	public int hashCode() {
		return endpointType.hashCode();
	}

}
