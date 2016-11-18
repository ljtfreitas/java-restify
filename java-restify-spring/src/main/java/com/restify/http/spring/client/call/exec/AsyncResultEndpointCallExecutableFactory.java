package com.restify.http.spring.client.call.exec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.scheduling.annotation.AsyncResult;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class AsyncResultEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<AsyncResult<T>, T, O> {

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(AsyncResult.class);
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
	public EndpointCallExecutable<AsyncResult<T>, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new AsyncResultEndpointCallExecutable(executable);
	}

	private class AsyncResultEndpointCallExecutable implements EndpointCallExecutable<AsyncResult<T>, O> {

		private final EndpointCallExecutable<T, O> delegate;

		private AsyncResultEndpointCallExecutable(EndpointCallExecutable<T, O> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public AsyncResult<T> execute(EndpointCall<O> call, Object[] args) {
			return new AsyncResult<>(delegate.execute(call, args));
		}
	}
}
