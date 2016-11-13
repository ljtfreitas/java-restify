package com.restify.http.client.request.async;

public interface EndpointCallSuccessCallback<T> {

	public void onSuccess(T response);
}
