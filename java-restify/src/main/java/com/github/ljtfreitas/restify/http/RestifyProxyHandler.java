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
package com.github.ljtfreitas.restify.http;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.call.EndpointMethodExecutor;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointType;
import com.github.ljtfreitas.restify.reflection.JavaDefaultMethodExecutor;

public class RestifyProxyHandler implements InvocationHandler {

	private final EndpointType endpointType;
	private final EndpointMethodExecutor endpointMethodExecutor;

	public RestifyProxyHandler(EndpointType endpointType, EndpointMethodExecutor endpointMethodExecutor) {
		this.endpointType = endpointType;
		this.endpointMethodExecutor = endpointMethodExecutor;
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
			return JavaDefaultMethodExecutor.execute(method, proxy, args);
		} catch (Throwable e) {
			throw new ProxyMethodException("Error on execution method [" + method + "], "
					+ "on proxy object type [" + proxy.getClass() + "], with method args " + Arrays.toString(args), e);
		}
	}

	private Object executeObjectMethod(Method method, Object proxy, Object[] args) {
		try {
			return method.invoke(this, args);
		} catch (Exception e) {
			throw new ProxyMethodException("Error on execution Object method [" + method + "], "
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
