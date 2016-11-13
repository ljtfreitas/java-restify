package com.restify.http.client.call.exec.guava;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.EndpointMethodParameter;
import com.restify.http.contract.metadata.EndpointMethodParameters;
import com.restify.http.contract.metadata.reflection.JavaType;

public class ListenableFutureCallbackEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<Void, T> {

	private final ListeningExecutorService executorService;

	public ListenableFutureCallbackEndpointCallExecutableFactory() {
		this(MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor()));
	}

	public ListenableFutureCallbackEndpointCallExecutableFactory(ListeningExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		EndpointMethodParameters parameters = endpointMethod.parameters();
		return endpointMethod.runnableAsync()
				&& (!parameters.callbacks(FutureCallback.class).isEmpty());
	}

	@Override
	public EndpointCallExecutable<Void, T> create(EndpointMethod endpointMethod) {
		JavaType responseType = callbackArgumentType(endpointMethod.parameters().callbacks());
		return new ListenableFutureCallbackEndpointMethodExecutable(responseType, endpointMethod.parameters().callbacks());
	}

	private JavaType callbackArgumentType(Collection<EndpointMethodParameter> callbackMethodParameters) {
		return callbackMethodParameters.stream()
				.filter(p -> p.javaType().is(FutureCallback.class))
					.findFirst()
						.map(p -> p.javaType().parameterized() ? p.javaType().as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class)
							.map(t -> JavaType.of(t))
								.orElseGet(() -> JavaType.of(Object.class));
	}

	private class ListenableFutureCallbackEndpointMethodExecutable implements EndpointCallExecutable<Void, T> {

		private final JavaType type;
		private final Collection<EndpointMethodParameter> callbackMethodParameters;

		private ListenableFutureCallbackEndpointMethodExecutable(JavaType type, Collection<EndpointMethodParameter> callbackMethodParameters) {
			this.type = type;
			this.callbackMethodParameters = callbackMethodParameters;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public Void execute(EndpointCall<T> call, Object[] args) {
			FutureCallback<T> callback = callbackParameter(args);

			ListenableFuture<T> future = executorService.submit(() -> call.execute());
			Futures.addCallback(future, callback);

			return null;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private FutureCallback<T> callbackParameter(Object[] args) {
			return callbackMethodParameters.stream()
					.filter(p -> p.javaType().is(FutureCallback.class))
						.findFirst()
							.map(p -> (FutureCallback) args[p.position()])
								.orElse(null);
		}
	}
}
