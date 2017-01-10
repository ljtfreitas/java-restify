package com.github.ljtfreitas.restify.cdi;

import java.util.logging.Logger;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptor;

class SimpleEndpointRequestInterceptor implements EndpointRequestInterceptor {

	private final Logger log = Logger.getLogger(SimpleEndpointRequestInterceptor.class.getCanonicalName());
	
	@Override
	public EndpointRequest intercepts(EndpointRequest endpointRequest) {
		log.info("Running request: " + endpointRequest);
		return endpointRequest;
	}
}
