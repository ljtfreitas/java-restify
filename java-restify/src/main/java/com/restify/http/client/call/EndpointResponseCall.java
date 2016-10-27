package com.restify.http.client.call;

import com.restify.http.client.request.EndpointRequest;
import com.restify.http.client.request.EndpointRequestExecutor;
import com.restify.http.client.response.EndpointResponse;

class EndpointResponseCall<T> implements EndpointCall<EndpointResponse<T>> {

	private final EndpointRequest endpointRequest;
	private final EndpointRequestExecutor endpointRequestExecutor;

	public EndpointResponseCall(EndpointRequest endpointRequest, EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequest = endpointRequest;
		this.endpointRequestExecutor = endpointRequestExecutor;
	}

	@Override
	public EndpointResponse<T> execute() {
		return endpointRequestExecutor.execute(endpointRequest);
	}
}
