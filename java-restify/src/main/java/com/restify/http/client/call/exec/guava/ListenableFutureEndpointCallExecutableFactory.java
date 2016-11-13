package com.restify.http.client.call.exec.guava;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class ListenableFutureEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<ListenableFuture<T>, T> {

	private final ListeningExecutorService executorService;

	public ListenableFutureEndpointCallExecutableFactory() {
		this(MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor()));
	}

	public ListenableFutureEndpointCallExecutableFactory(ListeningExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(ListenableFuture.class);
	}

	@Override
	public EndpointCallExecutable<ListenableFuture<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : String.class;

		return new ListenableFutureEndpointMethodExecutable(JavaType.of(responseType));
	}

	private class ListenableFutureEndpointMethodExecutable implements EndpointCallExecutable<ListenableFuture<T>, T> {

		private final JavaType type;

		private ListenableFutureEndpointMethodExecutable(JavaType type) {
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public ListenableFuture<T> execute(EndpointCall<T> call, Object[] args) {
			return executorService.submit(() -> call.execute());
		}
	}
}
