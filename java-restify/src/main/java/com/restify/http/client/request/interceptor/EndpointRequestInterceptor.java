package com.restify.http.client.request.interceptor;

import com.restify.http.client.request.EndpointRequest;

public interface EndpointRequestInterceptor {

	public EndpointRequest intercepts(EndpointRequest endpointRequest);
}
