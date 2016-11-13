package com.restify.http.client.call.async;

import java.util.concurrent.Executor;

import com.restify.http.client.call.EndpointCall;

public class AsyncEndpointCallFactory {

	public <T> AsyncEndpointCall<T> create(EndpointCall<T> call, Executor executor) {
		return new DefaultAsyncEndpointCall<T>(executor, call);
	}
}
