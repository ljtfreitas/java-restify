package com.restify.http.client.interceptor;

import java.util.Collection;
import java.util.LinkedList;

import com.restify.http.client.EndpointRequest;

public class EndpointRequestInterceptorStack {

	private final Collection<EndpointRequestInterceptor> interceptors = new LinkedList<>();

	public void add(EndpointRequestInterceptor interceptor) {
		interceptors.add(interceptor);
	}

	public EndpointRequest apply(EndpointRequest endpointRequest) {
		return interceptors.stream()
				.reduce(endpointRequest, (er, i) -> i.intercepts(er), (a, b) -> b);
	}
}
