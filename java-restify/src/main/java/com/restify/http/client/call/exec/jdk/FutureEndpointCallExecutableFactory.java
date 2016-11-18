package com.restify.http.client.call.exec.jdk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class FutureEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<Future<T>, T, O> {

	private final ExecutorService executorService;

	public FutureEndpointCallExecutableFactory() {
		this.executorService = Executors.newSingleThreadExecutor();
	}

	public FutureEndpointCallExecutableFactory(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Future.class);
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
	public EndpointCallExecutable<Future<T>, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new FutureEndpointCallExecutable(executable);
	}

	private class FutureEndpointCallExecutable implements EndpointCallExecutable<Future<T>, O> {

		private final EndpointCallExecutable<T, O> delegate;

		private FutureEndpointCallExecutable(EndpointCallExecutable<T, O> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Future<T> execute(EndpointCall<O> call, Object[] args) {
			return executorService.submit(() -> delegate.execute(call, args));
		}
	}
}
