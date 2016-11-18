package com.restify.http.spring.client.call.exec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.concurrent.ListenableFutureTask;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class ListenableFutureTaskEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<ListenableFutureTask<T>, T, O> {

	private final AsyncListenableTaskExecutor asyncListenableTaskExecutor;

	public ListenableFutureTaskEndpointCallExecutableFactory() {
		this(new SimpleAsyncTaskExecutor());
	}

	public ListenableFutureTaskEndpointCallExecutableFactory(AsyncListenableTaskExecutor asyncTaskExecutor) {
		this.asyncListenableTaskExecutor = asyncTaskExecutor;
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
		return new ListenableFutureTaskEndpointCallExecutable(executable);
	}

	private class ListenableFutureTaskEndpointCallExecutable implements EndpointCallExecutable<ListenableFutureTask<T>, O> {

		private final EndpointCallExecutable<T, O> delegate;

		public ListenableFutureTaskEndpointCallExecutable(EndpointCallExecutable<T, O> executable) {
			this.delegate = executable;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public ListenableFutureTask<T> execute(EndpointCall<O> call, Object[] args) {
			ListenableFutureTask<T> task = new ListenableFutureTask<T>(() -> delegate.execute(call, args));
			asyncListenableTaskExecutor.submit(task);
			return task;
		}
	}
}
