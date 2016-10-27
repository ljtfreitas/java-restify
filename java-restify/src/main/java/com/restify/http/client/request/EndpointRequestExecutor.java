package com.restify.http.client.request;

import com.restify.http.client.response.EndpointResponse;

public interface EndpointRequestExecutor {

	public <T> EndpointResponse<T> execute(EndpointRequest endpointRequest);

}
