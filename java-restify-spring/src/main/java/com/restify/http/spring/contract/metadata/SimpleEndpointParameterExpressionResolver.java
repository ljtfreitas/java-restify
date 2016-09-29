package com.restify.http.spring.contract.metadata;

public class SimpleEndpointParameterExpressionResolver implements EndpointParameterExpressionResolver {

	@Override
	public String resolve(String expression) {
		return expression;
	}

}
