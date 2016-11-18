package com.restify.http.client.call.exec.jdk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class CallableEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<Callable<T>, T, O> {

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Callable.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return JavaType.of(unwrap(endpointMethod.returnType()));
	}

	private Type unwrap(JavaType declaredReturnType) {
		return declaredReturnType.parameterized() ?
				declaredReturnType.as(ParameterizedType.class).getActualTypeArguments()[0] :
					Object.class;
	}

	@Override
	public EndpointCallExecutable<Callable<T>, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new CallableEndpointCallExecutable(executable);
	}

	private class CallableEndpointCallExecutable implements EndpointCallExecutable<Callable<T>, O> {

		private final EndpointCallExecutable<T, O> delegate;

		private CallableEndpointCallExecutable(EndpointCallExecutable<T, O> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Callable<T> execute(EndpointCall<O> call, Object[] args) {
			return () -> delegate.execute(call, args);
		}
	}
}
