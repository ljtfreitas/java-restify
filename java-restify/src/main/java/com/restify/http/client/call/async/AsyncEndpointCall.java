package com.restify.http.client.call.async;

import com.restify.http.client.request.async.EndpointCallCallback;
import com.restify.http.client.request.async.EndpointCallFailureCallback;
import com.restify.http.client.request.async.EndpointCallSuccessCallback;

public interface AsyncEndpointCall<T> {

	public void execute(EndpointCallCallback<T> callback);

	public void execute(EndpointCallSuccessCallback<T> success, EndpointCallFailureCallback failure);
}
