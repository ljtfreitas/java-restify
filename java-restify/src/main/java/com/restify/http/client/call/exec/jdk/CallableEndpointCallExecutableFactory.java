package com.restify.http.client.call.exec.jdk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class CallableEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<Callable<T>, T> {

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Callable.class);
	}

	@Override
	public EndpointCallExecutable<Callable<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new CallableEndpointCallExecutable(JavaType.of(responseType));
	}

	private class CallableEndpointCallExecutable implements EndpointCallExecutable<Callable<T>, T> {

		private final JavaType type;

		private CallableEndpointCallExecutable(JavaType type) {
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public Callable<T> execute(EndpointCall<T> call, Object[] args) {
			return () -> call.execute();
		}
	}
}
