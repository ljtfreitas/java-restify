package com.restify.http.client.call.exec.jdk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class CompletableFutureEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<CompletableFuture<T>, T, O> {

	private final Executor executor;

	public CompletableFutureEndpointCallExecutableFactory() {
		this(Executors.newSingleThreadExecutor());
	}

	public CompletableFutureEndpointCallExecutableFactory(Executor executor) {
		this.executor = executor;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(CompletableFuture.class);
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
	public EndpointCallExecutable<CompletableFuture<T>, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new CompletableFutureEndpointCallExecutable(executable);
	}

	private class CompletableFutureEndpointCallExecutable implements EndpointCallExecutable<CompletableFuture<T>, O> {

		private final EndpointCallExecutable<T, O> delegate;

		public CompletableFutureEndpointCallExecutable(EndpointCallExecutable<T, O> executable) {
			this.delegate = executable;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public CompletableFuture<T> execute(EndpointCall<O> call, Object[] args) {
			return CompletableFuture.supplyAsync(() -> delegate.execute(call, args), executor);
		}
	}
}
