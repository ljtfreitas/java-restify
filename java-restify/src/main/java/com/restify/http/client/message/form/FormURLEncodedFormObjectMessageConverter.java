package com.restify.http.client.message.form;

import java.lang.reflect.Type;
import java.util.Arrays;

import com.restify.http.client.response.RestifyHttpMessageReadException;
import com.restify.http.contract.Form;
import com.restify.http.contract.metadata.FormObjects;
import com.restify.http.contract.metadata.FormObjects.FormObject;
import com.restify.http.contract.metadata.reflection.JavaAnnotationScanner;

public class FormURLEncodedFormObjectMessageConverter extends FormURLEncodedMessageConverter<Object> {

	private final FormObjects formObjects = FormObjects.cache();

	@Override
	public boolean canRead(Type type) {
		return type instanceof Class && ((Class<?>) type).isAnnotationPresent(Form.class);
	}

	@Override
	protected Object doRead(Type expectedType, ParameterPair[] pairs) {
		Class<?> type = (Class<?>) expectedType;

		try {
			FormObject formObject = formObjects.of(type);

			Object result = type.newInstance();

			Arrays.stream(pairs)
				.forEach(p -> formObject.fieldBy(p.name()).ifPresent(f -> f.applyTo(result, p.value())));

			return result;

		} catch (InstantiationException | IllegalAccessException e) {
			throw new RestifyHttpMessageReadException(e);
		}
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return new JavaAnnotationScanner(type).contains(Form.class);
	}
}
