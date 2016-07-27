package com.restify.http.metadata;

import java.util.Map;

public class EndpointMethodQueryStringParameterSerializer implements EndpointMethodParameterSerializer {

	@SuppressWarnings("unchecked")
	@Override
	public String serialize(Object source) {
		return source instanceof Parameters ? serializeAsParameters((Parameters) source)
				: source instanceof Map ? serializeAsMap((Map<String, ?>) source)
					: source.toString();
	}

	private String serializeAsMap(Map<String, ?> source) {
		Parameters parameters = new Parameters();

		source.forEach((key, value) -> {
			if (value instanceof Iterable) {
				((Iterable<?>) value).forEach(o -> parameters.put(key, o.toString()));

			} else {
				parameters.put(key, value.toString());
			}
		});

		return serializeAsParameters(parameters);
	}

	private String serializeAsParameters(Parameters source) {
		return source.queryString();
	}
}
