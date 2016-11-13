package com.restify.http.client.call.exec.guava;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.common.base.Optional;
import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class OptionalEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<Optional<T>, T> {

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Optional.class);
	}

	@Override
	public EndpointCallExecutable<Optional<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new OptionalEndpointMethodExecutable(JavaType.of(responseType));
	}

	private class OptionalEndpointMethodExecutable implements EndpointCallExecutable<Optional<T>, T> {

		private final JavaType returnType;

		public OptionalEndpointMethodExecutable(JavaType returnType) {
			this.returnType = returnType;
		}

		@Override
		public JavaType returnType() {
			return returnType;
		}

		@Override
		public Optional<T> execute(EndpointCall<T> call, Object[] args) {
			return Optional.fromNullable(call.execute());
		}
	}
}
