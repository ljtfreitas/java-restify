package com.restify.http.client.call.exec.guava;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.EndpointMethodParameter;
import com.restify.http.contract.metadata.EndpointMethodParameters;
import com.restify.http.contract.metadata.reflection.JavaType;

public class ListenableFutureCallbackEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<Void, T, O> {

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
	public JavaType returnType(EndpointMethod endpointMethod) {
		return JavaType.of(callbackArgumentType(endpointMethod.parameters().callbacks()));
	}

	private Type callbackArgumentType(Collection<EndpointMethodParameter> callbackMethodParameters) {
		return callbackMethodParameters.stream()
				.filter(p -> p.javaType().is(FutureCallback.class))
					.findFirst()
						.map(p -> p.javaType().parameterized() ? p.javaType().as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class)
							.orElse(Object.class);
	}

	@Override
	public EndpointCallExecutable<Void, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new ListenableFutureCallbackEndpointMethodExecutable(endpointMethod.parameters().callbacks(), executable);
	}

	private class ListenableFutureCallbackEndpointMethodExecutable implements EndpointCallExecutable<Void, O> {

		private final Collection<EndpointMethodParameter> callbackMethodParameters;
		private final EndpointCallExecutable<T, O> delegate;

		public ListenableFutureCallbackEndpointMethodExecutable(Collection<EndpointMethodParameter> callbackMethodParameters,
				EndpointCallExecutable<T, O> executable) {
			this.callbackMethodParameters = callbackMethodParameters;
			this.delegate = executable;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Void execute(EndpointCall<O> call, Object[] args) {
			FutureCallback<T> callback = callbackParameter(args);

			ListenableFuture<T> future = executorService.submit(() -> delegate.execute(call, args));
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
