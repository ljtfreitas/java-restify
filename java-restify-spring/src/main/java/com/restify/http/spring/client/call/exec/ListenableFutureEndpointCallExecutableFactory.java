package com.restify.http.spring.client.call.exec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class ListenableFutureEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<ListenableFuture<T>, T> {

	private final AsyncListenableTaskExecutor asyncListenableTaskExecutor;

	public ListenableFutureEndpointCallExecutableFactory() {
		this(new SimpleAsyncTaskExecutor());
	}

	public ListenableFutureEndpointCallExecutableFactory(AsyncListenableTaskExecutor asyncTaskExecutor) {
		this.asyncListenableTaskExecutor = asyncTaskExecutor;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(ListenableFuture.class);
	}

	@Override
	public EndpointCallExecutable<ListenableFuture<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new ListenableFutureEndpointCallExecutable(JavaType.of(responseType));
	}

	private class ListenableFutureEndpointCallExecutable implements EndpointCallExecutable<ListenableFuture<T>, T> {

		private final JavaType type;

		private ListenableFutureEndpointCallExecutable(JavaType type) {
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public ListenableFuture<T> execute(EndpointCall<T> call, Object[] args) {
			return asyncListenableTaskExecutor.submitListenable(() -> call.execute());
		}
	}
}
