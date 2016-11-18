package com.restify.http.client.call.exec;

import com.restify.http.contract.metadata.EndpointMethod;

public interface EndpointCallExecutableProvider {

	public boolean supports(EndpointMethod endpointMethod);
}
