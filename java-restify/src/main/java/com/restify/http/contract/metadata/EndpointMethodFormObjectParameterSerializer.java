package com.restify.http.contract.metadata;

import java.lang.reflect.Type;

import com.restify.http.contract.Form;
import com.restify.http.contract.metadata.FormObjects.FormObject;

public class EndpointMethodFormObjectParameterSerializer implements EndpointMethodParameterSerializer {

	private final FormObjects formObjects = FormObjects.cache();

	@Override
	public String serialize(String name, Type type, Object source) {
		if (isFormObject(source)) {
			return doSerialize(source);
		} else {
			throw new IllegalArgumentException("EndpointMethodFormObjectParameterSerializer cannot serialize an object without annotation @Form.");
		}
	}

	private String doSerialize(Object source) {
		FormObject form = formObjects.of(source.getClass());
		return form.serialize(source);
	}

	private boolean isFormObject(Object source) {
		return source.getClass().isAnnotationPresent(Form.class);
	}

}
