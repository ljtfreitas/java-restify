package com.restify.http.metadata;

import static com.restify.http.metadata.Preconditions.isFalse;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.restify.http.contract.Form;
import com.restify.http.contract.Form.Field;

public class EndpointMethodFormObjectParameterSerializer implements EndpointMethodParameterSerializer {

	private final Map<Class<?>, FormObject> cache = new ConcurrentHashMap<>();

	@Override
	public String serialize(Object source) {
		return isFormObject(source) ? doSerialize(source) : source.toString();
	}

	private String doSerialize(Object source) {
		FormObject form = Optional.ofNullable(cache.get(source.getClass())).orElseGet(() -> formOf(source));

		return form.serialize(source);
	}

	private FormObject formOf(Object source) {
		Class<?> formObjectType = source.getClass();

		FormObject formObjectMetadata = new FormObject(formObjectType);

		Arrays.stream(formObjectType.getDeclaredFields())
			.filter(f -> f.getAnnotation(Field.class) != null)
				.forEach(f -> {
					Field annotation = f.getAnnotation(Field.class);
					String name = annotation.value().isEmpty() ? f.getName() : annotation.value();

					formObjectMetadata.put(name, f);
				});

		return formObjectMetadata;
	}

	private boolean isFormObject(Object source) {
		return source.getClass().getAnnotation(Form.class) != null;
	}

	private class FormObject {

		private final Class<?> type;
		private final Map<String, java.lang.reflect.Field> fields = new LinkedHashMap<>();

		private FormObject(Class<?> type) {
			this.type = type;
		}

		private String serialize(Object source) {
			Parameters parameters = new Parameters();

			fields.forEach((name, field) -> {
				field.setAccessible(true);

				try {
					parameters.put(name, field.get(source).toString());
				} catch (Exception e) {
					throw new UnsupportedOperationException(e);
				}
			});

			return parameters.queryString();
		}

		private void put(String name, java.lang.reflect.Field field) {
			isFalse(fields.containsKey(name), "Duplicate field [" + name + " on @Form object: " + type);
			fields.put(name, field);
		}
	}
}
