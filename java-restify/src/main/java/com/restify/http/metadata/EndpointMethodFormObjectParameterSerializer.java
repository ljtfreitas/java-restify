package com.restify.http.metadata;

import com.restify.http.contract.Form;
import com.restify.http.metadata.FormObjects.FormObject;

public class EndpointMethodFormObjectParameterSerializer implements EndpointMethodParameterSerializer {

	private final FormObjects formObjects = FormObjects.cache();

	@Override
	public String serialize(Object source) {
		return isFormObject(source) ? doSerialize(source) : source.toString();
	}

	private String doSerialize(Object source) {
		FormObject form = formObjects.of(source.getClass());
		return form.serialize(source);
	}

	private boolean isFormObject(Object source) {
		return source.getClass().getAnnotation(Form.class) != null;
	}

}
