package com.restify.http.metadata;

import java.util.Map;

import com.restify.http.contract.Form;

public class EndpointMethodQueryParametersSerializer implements EndpointMethodParameterSerializer {

	private EndpointMethodFormObjectParameterSerializer formObjectSerializer = new EndpointMethodFormObjectParameterSerializer();

	@SuppressWarnings("unchecked")
	@Override
	public String serialize(Object source) {
		if (source instanceof Parameters) {
			return serializeAsParameters((Parameters) source);

		} else if (source instanceof Map) {
			return serializeAsMap((Map<String, ?>) source);

		} else if (isFormObject(source)) {
			return serializeAsFormObject(source);

		} else {
			throw new IllegalArgumentException(
					"EndpointMethodQueryParametersSerializer does no support a parameter of type " + source.getClass());
		}
	}

	private String serializeAsFormObject(Object source) {
		return formObjectSerializer.serialize(source);
	}

	private boolean isFormObject(Object source) {
		return source.getClass().getAnnotation(Form.class) != null;
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
