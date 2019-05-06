package com.github.ljtfreitas.restify.http.call.handler;

import java.lang.reflect.Method;

import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;

class SimpleEndpointMethod extends EndpointMethod {

	SimpleEndpointMethod(Method javaMethod) {
		super(javaMethod, "/", "GET");
	}

	SimpleEndpointMethod(Method javaMethod, EndpointMethodParameters parameters) {
		super(javaMethod, "/", "GET", parameters);
	}
}
