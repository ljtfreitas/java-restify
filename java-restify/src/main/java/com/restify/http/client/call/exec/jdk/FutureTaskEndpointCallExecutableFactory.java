package com.restify.http.client.call.exec.jdk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class FutureTaskEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<FutureTask<T>, T> {

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
	public EndpointCallExecutable<FutureTask<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new FutureTaskEndpointCallExecutable(JavaType.of(responseType));
	}

	private class FutureTaskEndpointCallExecutable implements EndpointCallExecutable<FutureTask<T>, T> {

		private final JavaType type;

		private FutureTaskEndpointCallExecutable(JavaType type) {
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public FutureTask<T> execute(EndpointCall<T> call, Object[] args) {
			FutureTask<T> task = new FutureTask<T>(() -> call.execute());
			executorService.submit(task);
			return task;
		}
	}
}
