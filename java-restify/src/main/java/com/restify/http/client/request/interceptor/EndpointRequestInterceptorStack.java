package com.restify.http.client.request.interceptor;

import java.util.ArrayList;
import java.util.Collection;

import com.restify.http.client.request.EndpointRequest;

public class EndpointRequestInterceptorStack {

	private final Collection<EndpointRequestInterceptor> interceptors;

	public EndpointRequestInterceptorStack(Collection<EndpointRequestInterceptor> interceptors) {
		this.interceptors = new ArrayList<>(interceptors);
	}

	public EndpointRequest apply(EndpointRequest endpointRequest) {
		return interceptors.stream().reduce(endpointRequest, (r, i) -> i.intercepts(r), (a, b) -> b);
	}
}
