package com.restify.http.client.call.exec.async;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.async.AsyncEndpointCall;
import com.restify.http.client.call.async.AsyncEndpointCallFactory;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class AsyncEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<AsyncEndpointCall<T>, T> {

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
	public EndpointCallExecutable<AsyncEndpointCall<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new AsyncEndpointCallExecutable(JavaType.of(responseType));
	}

	private class AsyncEndpointCallExecutable implements EndpointCallExecutable<AsyncEndpointCall<T>, T> {

		private final JavaType type;

		private AsyncEndpointCallExecutable(JavaType type) {
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public AsyncEndpointCall<T> execute(EndpointCall<T> call, Object[] args) {
			return asyncEndpointCallFactory.create(call, executor);
		}
	}
}
