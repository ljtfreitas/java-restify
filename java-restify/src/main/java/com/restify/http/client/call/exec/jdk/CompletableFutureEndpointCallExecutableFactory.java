package com.restify.http.client.call.exec.jdk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class CompletableFutureEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<CompletableFuture<T>, T> {

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
	public EndpointCallExecutable<CompletableFuture<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new CompletableFutureEndpointMethodExecutable(executor, JavaType.of(responseType));
	}

	private class CompletableFutureEndpointMethodExecutable implements EndpointCallExecutable<CompletableFuture<T>, T> {

		private final Executor executor;
		private final JavaType type;

		private CompletableFutureEndpointMethodExecutable(Executor executor, JavaType type) {
			this.executor = executor;
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public CompletableFuture<T> execute(EndpointCall<T> call) {
			return CompletableFuture.supplyAsync(() -> call.execute(), executor);
		}
	}
}
