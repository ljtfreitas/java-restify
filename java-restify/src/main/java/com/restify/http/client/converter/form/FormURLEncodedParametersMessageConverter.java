package com.restify.http.client.converter.form;

import com.restify.http.metadata.Parameters;

public class FormURLEncodedParametersMessageConverter extends FormURLEncodedMessageConverter<Parameters> {

	@Override
	public boolean canWrite(Class<?> type) {
		return type == Parameters.class;
	}

	@Override
	public boolean canRead(Class<?> type) {
		return type == Parameters.class;
	}

	@Override
	protected Parameters doRead(ParameterPair[] pairs) {
		Parameters parameters = new Parameters();

		for (ParameterPair pair : pairs) {
			parameters.put(pair.name(), pair.value());
		}

		return parameters;
	}
}
