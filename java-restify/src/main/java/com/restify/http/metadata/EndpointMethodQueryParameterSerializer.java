package com.restify.http.metadata;

import java.lang.reflect.Type;

public class EndpointMethodQueryParameterSerializer implements EndpointMethodParameterSerializer {

	@SuppressWarnings("rawtypes")
	@Override
	public String serialize(String name, Type type, Object source) {
		if (source instanceof Iterable) {
			return serializeAsIterable(name, (Iterable) source);

		} else {
			return name + "=" + source.toString();
		}
	}

	@SuppressWarnings("rawtypes")
	private String serializeAsIterable(String name, Iterable source) {
		Parameters parameters = new Parameters();

		for (Object e : source) {
			parameters.put(name, e.toString());
		}

		return parameters.queryString();
	}
}
