package com.restify.http.spring.client.call.exec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class ListenableFutureEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<ListenableFuture<T>, T, O> {

	private final AsyncListenableTaskExecutor asyncListenableTaskExecutor;

	public ListenableFutureEndpointCallExecutableFactory() {
		this(new SimpleAsyncTaskExecutor("ListenableFutureEndpointCallExecutable"));
	}

	public ListenableFutureEndpointCallExecutableFactory(AsyncListenableTaskExecutor asyncTaskExecutor) {
		this.asyncListenableTaskExecutor = asyncTaskExecutor;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(ListenableFuture.class);
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
	public EndpointCallExecutable<ListenableFuture<T>, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new ListenableFutureEndpointCallExecutable(executable);
	}

	private class ListenableFutureEndpointCallExecutable implements EndpointCallExecutable<ListenableFuture<T>, O> {

		private final EndpointCallExecutable<T, O> delegate;

		public ListenableFutureEndpointCallExecutable(EndpointCallExecutable<T, O> executable) {
			this.delegate = executable;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public ListenableFuture<T> execute(EndpointCall<O> call, Object[] args) {
			return asyncListenableTaskExecutor.submitListenable(() -> delegate.execute(call, args));
		}
	}
}
