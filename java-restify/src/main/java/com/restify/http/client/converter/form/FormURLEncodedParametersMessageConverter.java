package com.restify.http.client.converter.form;

import java.lang.reflect.Type;

import com.restify.http.metadata.Parameters;

public class FormURLEncodedParametersMessageConverter extends FormURLEncodedMessageConverter<Parameters> {

	@Override
	public boolean canRead(Type type) {
		return type == Parameters.class;
	}

	@Override
	protected Parameters doRead(Type expectedType, ParameterPair[] pairs) {
		Parameters parameters = new Parameters();

		for (ParameterPair pair : pairs) {
			parameters.put(pair.name(), pair.value());
		}

		return parameters;
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return type == Parameters.class;
	}
}
