package com.restify.http.client.call.exec.async;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.async.AsyncEndpointCall;
import com.restify.http.client.call.async.AsyncEndpointCallFactory;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.client.request.async.EndpointCallFailureCallback;
import com.restify.http.client.request.async.EndpointCallSuccessCallback;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.EndpointMethodParameter;
import com.restify.http.contract.metadata.EndpointMethodParameters;
import com.restify.http.contract.metadata.reflection.JavaType;

public class AsyncCallbackEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<Void, T> {

	private final Executor executor;
	private final AsyncEndpointCallFactory asyncEndpointCallFactory;

	public AsyncCallbackEndpointCallExecutableFactory() {
		this(Executors.newSingleThreadExecutor());
	}

	public AsyncCallbackEndpointCallExecutableFactory(Executor executor) {
		this(executor, new AsyncEndpointCallFactory());
	}

	public AsyncCallbackEndpointCallExecutableFactory(Executor executor, AsyncEndpointCallFactory asyncEndpointCallFactory) {
		this.executor = executor;
		this.asyncEndpointCallFactory = asyncEndpointCallFactory;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		EndpointMethodParameters parameters = endpointMethod.parameters();
		return endpointMethod.runnableAsync()
				&& (!parameters.callbacks(EndpointCallSuccessCallback.class).isEmpty() || !parameters.callbacks(EndpointCallFailureCallback.class).isEmpty());
	}

	@Override
	public EndpointCallExecutable<Void, T> create(EndpointMethod endpointMethod) {
		JavaType responseType = callbackArgumentType(endpointMethod.parameters().callbacks());
		return new AsyncCallbackEndpointCallExecutable(responseType, endpointMethod.parameters().callbacks());
	}

	private JavaType callbackArgumentType(Collection<EndpointMethodParameter> callbackMethodParameters) {
		return callbackMethodParameters.stream()
				.filter(p -> EndpointCallSuccessCallback.class.isAssignableFrom(p.javaType().classType()))
					.findFirst()
						.map(p -> p.javaType().parameterized() ? p.javaType().as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class)
							.map(t -> JavaType.of(t))
								.orElseGet(() -> JavaType.of(Object.class));
	}

	private class AsyncCallbackEndpointCallExecutable implements EndpointCallExecutable<Void, T> {

		private final JavaType type;
		private final Collection<EndpointMethodParameter> callbackMethodParameters;

		private AsyncCallbackEndpointCallExecutable(JavaType type, Collection<EndpointMethodParameter> callbackMethodParameters) {
			this.type = type;
			this.callbackMethodParameters = callbackMethodParameters;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void execute(EndpointCall<T> call, Object[] args) {
			AsyncEndpointCall<T> asyncEndpointCall = asyncEndpointCallFactory.create(call, executor);

			EndpointCallSuccessCallback<T> successCallback = callback(EndpointCallSuccessCallback.class, args);
			EndpointCallFailureCallback failureCallback = callback(EndpointCallFailureCallback.class, args);

			asyncEndpointCall.execute(successCallback, failureCallback);

			return null;
		}

		@SuppressWarnings("unchecked")
		private <P> P callback(Class<P> parameterClassType, Object[] args) {
			return callbackMethodParameters.stream()
					.filter(p -> parameterClassType.isAssignableFrom(p.javaType().classType()))
						.findFirst()
							.map(p -> (P) args[p.position()])
								.orElse(null);
		}
	}
}
