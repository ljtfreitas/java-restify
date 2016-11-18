package com.restify.http.client.call.exec.guava;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class ListenableFutureTaskEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<ListenableFutureTask<T>, T, O> {

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
	public JavaType returnType(EndpointMethod endpointMethod) {
		return JavaType.of(unwrap(endpointMethod.returnType()));
	}

	private Type unwrap(JavaType declaredReturnType) {
		return declaredReturnType.parameterized() ?
				declaredReturnType.as(ParameterizedType.class).getActualTypeArguments()[0] :
					Object.class;
	}

	@Override
	public EndpointCallExecutable<ListenableFutureTask<T>, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new ListenableFutureTaskEndpointMethodExecutable(executable);
	}

	private class ListenableFutureTaskEndpointMethodExecutable implements EndpointCallExecutable<ListenableFutureTask<T>, O> {

		private final EndpointCallExecutable<T, O> delegate;

		public ListenableFutureTaskEndpointMethodExecutable(EndpointCallExecutable<T, O> executable) {
			this.delegate = executable;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public ListenableFutureTask<T> execute(EndpointCall<O> call, Object[] args) {
			ListenableFutureTask<T> task = ListenableFutureTask.create(() -> delegate.execute(call, args));
			executorService.submit(task);
			return task;
		}
	}
}
