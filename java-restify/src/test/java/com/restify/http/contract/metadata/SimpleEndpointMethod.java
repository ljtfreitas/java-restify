package com.restify.http.contract.metadata;

import java.lang.reflect.Method;

public class SimpleEndpointMethod extends EndpointMethod {

	public SimpleEndpointMethod(Method javaMethod) {
		super(javaMethod, "/", "GET");
	}
}
