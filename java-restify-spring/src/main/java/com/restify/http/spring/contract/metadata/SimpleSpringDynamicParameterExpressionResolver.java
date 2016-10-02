package com.restify.http.spring.contract.metadata;

public class SimpleSpringDynamicParameterExpressionResolver implements SpringDynamicParameterExpressionResolver {

	@Override
	public String resolve(String expression) {
		return expression;
	}

}
