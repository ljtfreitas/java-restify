package com.restify.http.client.converter.form;

import java.lang.reflect.Type;
import java.util.Map;

public class FormURLEncodedMapMessageConverter extends FormURLEncodedMessageConverter<Map<String, ?>> {

	@Override
	public boolean canWrite(Class<?> type) {
		return type == Map.class;
	}

	@Override
	public boolean canRead(Type type) {
		return false;
	}
}
