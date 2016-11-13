package com.restify.http.client.call.exec.guava;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class ListenableFutureTaskEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<ListenableFutureTask<T>, T> {

	private final ListeningExecutorService executorService;

	public ListenableFutureTaskEndpointCallExecutableFactory() {
		this(MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor()));
	}

	public ListenableFutureTaskEndpointCallExecutableFactory(ListeningExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(ListenableFutureTask.class);
	}

	@Override
	public EndpointCallExecutable<ListenableFutureTask<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : String.class;

		return new ListenableFutureTaskEndpointMethodExecutable(JavaType.of(responseType));
	}

	private class ListenableFutureTaskEndpointMethodExecutable implements EndpointCallExecutable<ListenableFutureTask<T>, T> {

		private final JavaType type;

		private ListenableFutureTaskEndpointMethodExecutable(JavaType type) {
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public ListenableFutureTask<T> execute(EndpointCall<T> call, Object[] args) {
			ListenableFutureTask<T> task = ListenableFutureTask.create(() -> call.execute());
			executorService.submit(task);
			return task;
		}
	}
}
