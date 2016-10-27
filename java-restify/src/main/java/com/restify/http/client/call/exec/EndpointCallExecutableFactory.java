package com.restify.http.client.call.exec;

import com.restify.http.contract.metadata.EndpointMethod;

public interface EndpointCallExecutableFactory<M, T> {

	public boolean supports(EndpointMethod endpointMethod);

	public EndpointCallExecutable<M, T> create(EndpointMethod endpointMethod);
}
