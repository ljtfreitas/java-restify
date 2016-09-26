package com.restify.http.contract.metadata;

import java.lang.reflect.Type;

import com.restify.http.client.charset.Encoding;
import com.restify.http.contract.Parameters;

public class EndpointMethodQueryParameterSerializer implements EndpointMethodParameterSerializer {

	@SuppressWarnings("rawtypes")
	@Override
	public String serialize(String name, Type type, Object source) {
		if (source instanceof Iterable) {
			return serializeAsIterable(name, (Iterable) source);

		} else {
			return encode(name) + "=" + encode(source.toString());
		}
	}

	private String encode(String value) {
		return Encoding.UTF_8.encode(value);
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
