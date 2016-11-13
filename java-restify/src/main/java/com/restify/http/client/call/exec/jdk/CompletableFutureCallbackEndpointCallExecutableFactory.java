package com.restify.http.client.call.exec.jdk;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.EndpointMethodParameter;
import com.restify.http.contract.metadata.EndpointMethodParameters;
import com.restify.http.contract.metadata.reflection.JavaType;

public class CompletableFutureCallbackEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<Void, T> {

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
	public EndpointCallExecutable<Void, T> create(EndpointMethod endpointMethod) {
		JavaType responseType = callbackArgumentType(endpointMethod.parameters().callbacks());
		return new CompletableFutureCallbackEndpointCallExecutable(responseType, endpointMethod.parameters().callbacks());
	}

	private JavaType callbackArgumentType(Collection<EndpointMethodParameter> callbackMethodParameters) {
		return callbackMethodParameters.stream()
				.filter(p -> p.javaType().is(BiConsumer.class))
					.findFirst()
						.map(p -> p.javaType().parameterized() ? p.javaType().as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class)
							.map(t -> JavaType.of(t))
								.orElseGet(() -> JavaType.of(Object.class));
	}

	private class CompletableFutureCallbackEndpointCallExecutable implements EndpointCallExecutable<Void, T> {

		private final JavaType type;
		private final Collection<EndpointMethodParameter> callbackMethodParameters;

		private CompletableFutureCallbackEndpointCallExecutable(JavaType type, Collection<EndpointMethodParameter> callbackMethodParameters) {
			this.type = type;
			this.callbackMethodParameters = callbackMethodParameters;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public Void execute(EndpointCall<T> call, Object[] args) {
			BiConsumer<? super T, ? super Throwable> callback = callbackParameter(args);

			CompletableFuture.supplyAsync(() -> call.execute(), executor).whenComplete(callback);

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
