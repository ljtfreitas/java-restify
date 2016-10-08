package com.restify.http.client;

import com.restify.http.client.request.EndpointRequest;
import com.restify.http.client.request.EndpointRequestExecutor;
import com.restify.http.client.request.EndpointRequestFactory;
import com.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;
import com.restify.http.contract.metadata.EndpointMethod;

public class EndpointMethodExecutor {

	private final EndpointRequestInterceptorStack endpointRequestInterceptors;
	private final EndpointRequestExecutor endpointRequestExecutor;

	public EndpointMethodExecutor(EndpointRequestInterceptorStack endpointRequestInterceptors,
			EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequestInterceptors = endpointRequestInterceptors;
		this.endpointRequestExecutor = endpointRequestExecutor;
	}

	public Object execute(EndpointMethod endpointMethod, Object[] args) {
		EndpointRequest endpointRequest = endpointRequestInterceptors.apply(newRequest(endpointMethod, args));
		return endpointRequestExecutor.execute(endpointRequest);
	}

	private EndpointRequest newRequest(EndpointMethod endpointMethod, Object[] args) {
		return new EndpointRequestFactory(endpointMethod).createWith(args);
	}
}
