package com.restify.http.spring.client.call.exec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.concurrent.ListenableFutureTask;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class ListenableFutureTaskEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<ListenableFutureTask<T>, T> {

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
	public EndpointCallExecutable<ListenableFutureTask<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new ListenableFutureTaskEndpointCallExecutable(JavaType.of(responseType));
	}

	private class ListenableFutureTaskEndpointCallExecutable implements EndpointCallExecutable<ListenableFutureTask<T>, T> {

		private final JavaType type;

		private ListenableFutureTaskEndpointCallExecutable(JavaType type) {
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public ListenableFutureTask<T> execute(EndpointCall<T> call, Object[] args) {
			ListenableFutureTask<T> task = new ListenableFutureTask<T>(() -> call.execute());
			asyncListenableTaskExecutor.submit(task);
			return task;
		}
	}
}
