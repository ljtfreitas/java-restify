package com.restify.http.metadata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import com.restify.http.contract.Form;

public class EndpointMethodQueryParametersSerializer implements EndpointMethodParameterSerializer {

	private EndpointMethodFormObjectParameterSerializer formObjectSerializer = new EndpointMethodFormObjectParameterSerializer();

	@SuppressWarnings("rawtypes")
	@Override
	public String serialize(String name, Type type, Object source) {
		if (source instanceof Parameters) {
			return serializeAsParameters((Parameters) source);

		} else if (source instanceof Map && supportedMapKey(type)) {
			return serializeAsMap((Map) source);

		} else if (isFormObject(source)) {
			return serializeAsFormObject(name, type, source);

		} else {
			throw new IllegalArgumentException(
					"EndpointMethodQueryParametersSerializer does no support a parameter of type " + source.getClass());
		}
	}

	private boolean supportedMapKey(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;

			Type mapKeyType = parameterizedType.getActualTypeArguments()[0];

			if (mapKeyType == String.class) {
				return true;

			} else {
				throw new IllegalStateException("Your Map parameter must have a key of String type.");
			}
		} else {
			return true;
		}
	}

	private String serializeAsFormObject(String name, Type type, Object source) {
		return formObjectSerializer.serialize(name, type, source);
	}

	private boolean isFormObject(Object source) {
		return source.getClass().getAnnotation(Form.class) != null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String serializeAsMap(Map source) {
		Parameters parameters = new Parameters();

		source.forEach((key, value) -> {
			if (value instanceof Iterable) {
				((Iterable) value).forEach(o -> parameters.put(key.toString(), o.toString()));

			} else {
				parameters.put(key.toString(), value.toString());
			}
		});

		return serializeAsParameters(parameters);
	}

	private String serializeAsParameters(Parameters source) {
		return source.queryString();
	}
}
