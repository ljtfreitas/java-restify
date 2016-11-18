package com.restify.http.client.call.exec.async;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.async.AsyncEndpointCall;
import com.restify.http.client.call.async.AsyncEndpointCallFactory;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class AsyncEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<AsyncEndpointCall<T>, T, O> {

	private final Executor executor;
	private final AsyncEndpointCallFactory asyncEndpointCallFactory;

	public AsyncEndpointCallExecutableFactory() {
		this(Executors.newSingleThreadExecutor());
	}

	public AsyncEndpointCallExecutableFactory(Executor executor) {
		this(executor, new AsyncEndpointCallFactory());
	}

	public AsyncEndpointCallExecutableFactory(Executor executor, AsyncEndpointCallFactory asyncEndpointCallFactory) {
		this.executor = executor;
		this.asyncEndpointCallFactory = asyncEndpointCallFactory;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(AsyncEndpointCall.class);
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
	public EndpointCallExecutable<AsyncEndpointCall<T>, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new AsyncEndpointCallExecutable(executable);
	}

	private class AsyncEndpointCallExecutable implements EndpointCallExecutable<AsyncEndpointCall<T>, O> {

		private final EndpointCallExecutable<T, O> delegate;

		private AsyncEndpointCallExecutable(EndpointCallExecutable<T, O> executable) {
			this.delegate = executable;;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public AsyncEndpointCall<T> execute(EndpointCall<O> call, Object[] args) {
			return asyncEndpointCallFactory.create(() -> delegate.execute(call, args), executor);
		}
	}
}
