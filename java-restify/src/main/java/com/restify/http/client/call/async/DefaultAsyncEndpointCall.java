package com.restify.http.client.call.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.request.async.EndpointCallCallback;
import com.restify.http.client.request.async.EndpointCallFailureCallback;
import com.restify.http.client.request.async.EndpointCallSuccessCallback;

public class DefaultAsyncEndpointCall<T> implements AsyncEndpointCall<T> {

	private final Executor executor;
	private final EndpointCall<T> source;

	public DefaultAsyncEndpointCall(Executor executor, EndpointCall<T> source) {
		this.executor = executor;
		this.source = source;
	}

	@Override
	public void execute(EndpointCallCallback<T> callback) {
		CompletableFuture
			.supplyAsync(() -> source.execute(), executor)
				.whenComplete((r, e) -> handle(r, e, callback, callback));
	}

	@Override
	public void execute(EndpointCallSuccessCallback<T> successCallback, EndpointCallFailureCallback failureCallback) {
		CompletableFuture
			.supplyAsync(() -> source.execute(), executor)
				.whenComplete((r, e) -> handle(r, e, successCallback, failureCallback));
	}

	private void handle(T value, Throwable throwable, EndpointCallSuccessCallback<T> successCallback, EndpointCallFailureCallback failureCallback) {
		if (value != null && successCallback != null) {
			successCallback.onSuccess(value);

		} else if (throwable != null && failureCallback != null) {
			failureCallback.onFailure(throwable);
		}
	}
}
