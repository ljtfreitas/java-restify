package com.restify.http.client.call.exec.jdk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class FutureTaskEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<FutureTask<T>, T, O> {

	private final ExecutorService executorService;

	public FutureTaskEndpointCallExecutableFactory() {
		this.executorService = Executors.newSingleThreadExecutor();
	}

	public FutureTaskEndpointCallExecutableFactory(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(FutureTask.class);
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
	public EndpointCallExecutable<FutureTask<T>, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new FutureTaskEndpointCallExecutable(executable);
	}

	private class FutureTaskEndpointCallExecutable implements EndpointCallExecutable<FutureTask<T>, O> {

		private final EndpointCallExecutable<T, O> delegate;

		public FutureTaskEndpointCallExecutable(EndpointCallExecutable<T, O> executable) {
			this.delegate = executable;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public FutureTask<T> execute(EndpointCall<O> call, Object[] args) {
			FutureTask<T> task = new FutureTask<T>(() -> delegate.execute(call, args));
			executorService.submit(task);
			return task;
		}
	}
}
