package com.restify.http.client;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.EndpointCallFactory;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutables;
import com.restify.http.contract.metadata.EndpointMethod;

public class EndpointMethodExecutor {

	private final EndpointCallExecutables endpointMethodExecutables;
	private final EndpointCallFactory endpointMethodCallFactory;

	public EndpointMethodExecutor(EndpointCallExecutables endpointMethodExecutables, EndpointCallFactory endpointMethodCallFactory) {
		this.endpointMethodExecutables = endpointMethodExecutables;
		this.endpointMethodCallFactory = endpointMethodCallFactory;
	}

	public Object execute(EndpointMethod endpointMethod, Object[] args) {
		EndpointCallExecutable<Object, Object> executable = endpointMethodExecutables.of(endpointMethod);

		EndpointCall<Object> call = endpointMethodCallFactory.createWith(endpointMethod, args, executable.returnType());

		return executable.execute(call);
	}
}
