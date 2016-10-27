package com.restify.http.client.call;

import com.restify.http.client.request.EndpointRequest;
import com.restify.http.client.request.EndpointRequestExecutor;

class DefaultEndpointCall<T> implements EndpointCall<T> {

	private final EndpointRequest endpointRequest;
	private final EndpointRequestExecutor endpointRequestExecutor;

	public DefaultEndpointCall(EndpointRequest endpointRequest, EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequest = endpointRequest;
		this.endpointRequestExecutor = endpointRequestExecutor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T execute() {
		return (T) endpointRequestExecutor.execute(endpointRequest).body();
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("EndpointCall: [")
				.append("Request: ")
					.append(endpointRequest)
			.append("]");

		return report.toString();
	}
}
