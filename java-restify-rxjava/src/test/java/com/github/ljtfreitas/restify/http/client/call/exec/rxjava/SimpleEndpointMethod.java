package com.github.ljtfreitas.restify.http.client.call.exec.rxjava;

import java.lang.reflect.Method;

import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;

public class SimpleEndpointMethod extends EndpointMethod {

	public SimpleEndpointMethod(Method javaMethod) {
		super(javaMethod, "/", "GET");
	}

	public SimpleEndpointMethod(Method javaMethod, EndpointMethodParameters parameters) {
		super(javaMethod, "/", "GET", parameters);
	}
}
