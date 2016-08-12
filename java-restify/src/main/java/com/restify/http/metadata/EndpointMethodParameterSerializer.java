package com.restify.http.metadata;

import java.lang.reflect.Type;

public interface EndpointMethodParameterSerializer {

	public String serialize(String name, Type type, Object source);
}
