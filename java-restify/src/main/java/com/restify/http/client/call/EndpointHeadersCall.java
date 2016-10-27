package com.restify.http.client.call;

import com.restify.http.client.Headers;
import com.restify.http.client.request.EndpointRequest;
import com.restify.http.client.request.EndpointRequestExecutor;

class EndpointHeadersCall implements EndpointCall<Headers> {

	private final EndpointRequest endpointRequest;
	private final EndpointRequestExecutor endpointRequestExecutor;

	public EndpointHeadersCall(EndpointRequest endpointRequest, EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequest = endpointRequest;
		this.endpointRequestExecutor = endpointRequestExecutor;
	}

	@Override
	public Headers execute() {
		return endpointRequestExecutor.execute(endpointRequest).headers();
	}

}
