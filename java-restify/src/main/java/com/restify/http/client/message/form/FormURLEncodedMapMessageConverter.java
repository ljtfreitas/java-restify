package com.restify.http.client.message.form;

import java.lang.reflect.Type;
import java.util.Map;

public class FormURLEncodedMapMessageConverter extends FormURLEncodedMessageConverter<Map<String, ?>> {

	@Override
	public boolean canRead(Type type) {
		return false;
	}

	@Override
	protected Map<String, ?> doRead(Type expectedType, ParameterPair[] pairs) {
		throw new UnsupportedOperationException("Cannot read HTTP response to Map type.");
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return Map.class.isAssignableFrom(type);
	}
}
