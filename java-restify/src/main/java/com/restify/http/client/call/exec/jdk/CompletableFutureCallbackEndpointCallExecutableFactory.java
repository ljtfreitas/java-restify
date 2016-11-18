package com.restify.http.client.call.exec.jdk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.EndpointMethodParameter;
import com.restify.http.contract.metadata.EndpointMethodParameters;
import com.restify.http.contract.metadata.reflection.JavaType;

public class CompletableFutureCallbackEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<Void, T, O> {

	private final Executor executor;

	public CompletableFutureCallbackEndpointCallExecutableFactory() {
		this(Executors.newSingleThreadExecutor());
	}

	public CompletableFutureCallbackEndpointCallExecutableFactory(Executor executor) {
		this.executor = executor;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		EndpointMethodParameters parameters = endpointMethod.parameters();
		return endpointMethod.runnableAsync()
				&& (!parameters.callbacks(BiConsumer.class).isEmpty());
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return JavaType.of(callbackArgumentType(endpointMethod.parameters().callbacks()));
	}

	private Type callbackArgumentType(Collection<EndpointMethodParameter> callbackMethodParameters) {
		return callbackMethodParameters.stream()
				.filter(p -> p.javaType().is(BiConsumer.class))
					.findFirst()
						.map(p -> p.javaType().parameterized() ? p.javaType().as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class)
							.orElse(Object.class);
	}

	@Override
	public EndpointCallExecutable<Void, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new CompletableFutureCallbackEndpointCallExecutable(endpointMethod.parameters().callbacks(), executable);
	}

	private class CompletableFutureCallbackEndpointCallExecutable implements EndpointCallExecutable<Void, O> {

		private final Collection<EndpointMethodParameter> callbackMethodParameters;
		private final EndpointCallExecutable<T, O> delegate;

		private CompletableFutureCallbackEndpointCallExecutable(Collection<EndpointMethodParameter> callbackMethodParameters, EndpointCallExecutable<T, O> executable) {
			this.callbackMethodParameters = callbackMethodParameters;
			this.delegate = executable;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Void execute(EndpointCall<O> call, Object[] args) {
			BiConsumer<? super T, ? super Throwable> callback = callbackParameter(args);

			CompletableFuture.supplyAsync(() -> delegate.execute(call, args), executor).whenComplete(callback);

			return null;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private BiConsumer<? super T, ? super Throwable> callbackParameter(Object[] args) {
			return callbackMethodParameters.stream()
					.filter(p -> p.javaType().is(BiConsumer.class))
						.findFirst()
							.map(p -> (BiConsumer) args[p.position()])
								.orElse(null);
		}
	}
}
