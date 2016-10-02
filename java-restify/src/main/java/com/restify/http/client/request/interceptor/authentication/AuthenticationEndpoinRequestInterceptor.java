package com.restify.http.client.request.interceptor.authentication;

import java.util.Optional;

import com.restify.http.client.authentication.Authentication;
import com.restify.http.client.request.EndpointRequest;
import com.restify.http.client.request.interceptor.EndpointRequestInterceptor;

public class AuthenticationEndpoinRequestInterceptor implements EndpointRequestInterceptor {

	private final Authentication authentication;

	public AuthenticationEndpoinRequestInterceptor(Authentication authentication) {
		this.authentication = authentication;
	}

	@Override
	public EndpointRequest intercepts(EndpointRequest endpointRequest) {
		Optional.ofNullable(authentication.content())
			.filter(a -> !a.isEmpty())
				.ifPresent(a -> endpointRequest.headers().put("Authorization", a));

		return endpointRequest;
	}
}
