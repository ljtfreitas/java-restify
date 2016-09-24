package com.restify.http.client;

import com.restify.http.client.request.EndpointRequest;
import com.restify.http.client.request.EndpointRequestExecutor;
import com.restify.http.client.request.EndpointRequestFactory;
import com.restify.http.contract.metadata.EndpointMethod;

public class EndpointMethodExecutor {

	private final EndpointRequestExecutor endpointRequestExecutor;

	public EndpointMethodExecutor(EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequestExecutor = endpointRequestExecutor;
	}

	public Object execute(EndpointMethod endpointMethod, Object[] args) {
		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).createWith(args);

		return endpointRequestExecutor.execute(endpointRequest);
	}
}
