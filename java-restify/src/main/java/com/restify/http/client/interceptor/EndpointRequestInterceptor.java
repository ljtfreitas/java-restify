package com.restify.http.client.interceptor;

import com.restify.http.client.EndpointRequest;

public interface EndpointRequestInterceptor {

	public EndpointRequest intercepts(EndpointRequest endpointRequest);
}
