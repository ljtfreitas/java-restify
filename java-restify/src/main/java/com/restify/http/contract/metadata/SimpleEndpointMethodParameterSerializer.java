package com.restify.http.contract.metadata;

import java.lang.reflect.Type;

public class SimpleEndpointMethodParameterSerializer implements EndpointMethodParameterSerializer {

	@Override
	public String serialize(String name, Type type, Object source) {
		return source.toString();
	}

}
