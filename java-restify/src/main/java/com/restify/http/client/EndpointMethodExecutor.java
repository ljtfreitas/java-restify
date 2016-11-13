package com.restify.http.client;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.EndpointCallFactory;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutables;
import com.restify.http.contract.metadata.EndpointMethod;

public class EndpointMethodExecutor {

	private final EndpointCallExecutables endpointCallExecutables;
	private final EndpointCallFactory endpointCallFactory;

	public EndpointMethodExecutor(EndpointCallExecutables endpointCallExecutables, EndpointCallFactory endpointCallFactory) {
		this.endpointCallExecutables = endpointCallExecutables;
		this.endpointCallFactory = endpointCallFactory;
	}

	public Object execute(EndpointMethod endpointMethod, Object[] args) {
		EndpointCallExecutable<Object, Object> executable = endpointCallExecutables.of(endpointMethod);

		EndpointCall<Object> call = endpointCallFactory.createWith(endpointMethod, args, executable.returnType());

		return executable.execute(call, args);
	}
}
