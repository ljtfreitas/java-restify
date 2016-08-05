package com.restify.http.client.interceptor.authentication;

import com.restify.http.client.EndpointRequest;
import com.restify.http.client.authentication.Authentication;
import com.restify.http.client.interceptor.EndpointRequestInterceptor;

public class EndpoinRequestAuthenticationInterceptor implements EndpointRequestInterceptor {

	private final Authentication authentication;

	public EndpoinRequestAuthenticationInterceptor(Authentication authentication) {
		this.authentication = authentication;
	}

	@Override
	public EndpointRequest intercepts(EndpointRequest endpointRequest) {
		endpointRequest.headers().put("Authorization", authentication.content());
		return endpointRequest;
	}

}
