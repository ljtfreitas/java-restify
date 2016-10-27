package com.restify.http.client.call.exec.jdk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class FutureEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<Future<T>, T> {

	private final ExecutorService executorService;

	public FutureEndpointCallExecutableFactory() {
		this.executorService = Executors.newSingleThreadExecutor();
	}

	public FutureEndpointCallExecutableFactory(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Future.class);
	}

	@Override
	public EndpointCallExecutable<Future<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new FutureEndpointMethodExecutable(executorService, JavaType.of(responseType));
	}

	private class FutureEndpointMethodExecutable implements EndpointCallExecutable<Future<T>, T> {

		private final ExecutorService executorService;
		private final JavaType type;

		private FutureEndpointMethodExecutable(ExecutorService executorService, JavaType type) {
			this.executorService = executorService;
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public Future<T> execute(EndpointCall<T> call) {
			return executorService.submit(() -> call.execute());
		}
	}
}
