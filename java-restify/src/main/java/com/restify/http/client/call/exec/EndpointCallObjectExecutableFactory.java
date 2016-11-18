package com.restify.http.client.call.exec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class EndpointCallObjectExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<EndpointCall<T>, T, O> {

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(EndpointCall.class);
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
	public EndpointCallExecutable<EndpointCall<T>, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new EndpointCallObjectExecutable(executable);
	}

	private class EndpointCallObjectExecutable implements EndpointCallExecutable<EndpointCall<T>, O> {

		private final EndpointCallExecutable<T, O> delegate;

		public EndpointCallObjectExecutable(EndpointCallExecutable<T, O> executable) {
			this.delegate = executable;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public EndpointCall<T> execute(EndpointCall<O> call, Object[] args) {
			return () -> delegate.execute(call, args);
		}
	}
}
