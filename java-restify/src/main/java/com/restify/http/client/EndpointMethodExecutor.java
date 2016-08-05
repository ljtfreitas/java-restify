package com.restify.http.client;

import com.restify.http.metadata.EndpointMethod;

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
