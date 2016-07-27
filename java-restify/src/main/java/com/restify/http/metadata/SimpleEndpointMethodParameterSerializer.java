package com.restify.http.metadata;

public class SimpleEndpointMethodParameterSerializer implements EndpointMethodParameterSerializer {

	@Override
	public String serialize(Object source) {
		return source.toString();
	}

}
