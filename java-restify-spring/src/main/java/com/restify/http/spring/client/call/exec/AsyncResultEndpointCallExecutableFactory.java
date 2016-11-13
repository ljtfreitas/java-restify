package com.restify.http.spring.client.call.exec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.scheduling.annotation.AsyncResult;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class AsyncResultEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<AsyncResult<T>, T> {

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(AsyncResult.class);
	}

	@Override
	public EndpointCallExecutable<AsyncResult<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new AsyncResultEndpointCallExecutable(JavaType.of(responseType));
	}

	private class AsyncResultEndpointCallExecutable implements EndpointCallExecutable<AsyncResult<T>, T> {

		private final JavaType returnType;

		private AsyncResultEndpointCallExecutable(JavaType returnType) {
			this.returnType = returnType;
		}

		@Override
		public JavaType returnType() {
			return returnType;
		}

		@Override
		public AsyncResult<T> execute(EndpointCall<T> call, Object[] args) {
			return new AsyncResult<>(call.execute());
		}
	}
}
