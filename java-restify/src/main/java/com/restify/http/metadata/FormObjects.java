package com.restify.http.metadata;

import static com.restify.http.metadata.Preconditions.isFalse;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.restify.http.contract.Form.Field;

public class FormObjects {

	private final Map<Class<?>, FormObject> cache = new ConcurrentHashMap<>();

	private FormObjects() {
	}

	public FormObject of(Class<?> formObjectType) {
		return get(formObjectType).orElseGet(() -> create(formObjectType));
	}

	private FormObject create(Class<?> formObjectType) {
		FormObject formObjectMetadata = new FormObject(formObjectType);

		Arrays.stream(formObjectType.getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(Field.class))
			.forEach(f -> {
				Field annotation = f.getAnnotation(Field.class);
				String name = annotation.value().isEmpty() ? f.getName() : annotation.value();

				formObjectMetadata.put(name, f);
		});

		cache.put(formObjectType, formObjectMetadata);

		return formObjectMetadata;
	}

	private Optional<FormObject> get(Class<?> formObjectType) {
		return Optional.ofNullable(cache.get(formObjectType));
	}

	private static final FormObjects singleton = new FormObjects();

	public static FormObjects cache() {
		return singleton;
	}

	public static class FormObject {

		private final Class<?> type;
		private final Map<String, java.lang.reflect.Field> fields = new LinkedHashMap<>();

		private FormObject(Class<?> type) {
			this.type = type;
		}

		public Optional<FormObjectField> fieldBy(String name) {
			return Optional.ofNullable(fields.get(name))
					.map(f -> new FormObjectField(f));
		}

		public String serialize(Object source) {
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

		public class FormObjectField {

			private final java.lang.reflect.Field field;

			FormObjectField(java.lang.reflect.Field field) {
				this.field = field;
			}

			public void applyTo(Object object, Object value) {
				try {
					field.setAccessible(true);
					field.set(object, valueOf(value));

				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new UnsupportedOperationException(e);
				}
			}

			private Object valueOf(Object value) {
				Class<?> fieldType = field.getType();

				if (fieldType.isInstance(value)) {
					return value;

				} else if (fieldType == Byte.class || fieldType == byte.class) {
					return Byte.valueOf(value.toString());

				} else if (fieldType == Short.class || fieldType == short.class) {
					return Short.valueOf(value.toString());

				} else if (fieldType == Integer.class || fieldType == int.class) {
					return Integer.valueOf(value.toString());

				} else if (fieldType == Long.class || fieldType == long.class) {
					return Long.valueOf(value.toString());

				} else if (fieldType == Float.class || fieldType == float.class) {
					return Float.valueOf(value.toString());

				} else if (fieldType == Double.class || fieldType == double.class) {
					return Integer.valueOf(value.toString());

				} else if (fieldType == Boolean.class || fieldType == boolean.class) {
					return Integer.valueOf(value.toString());

				} else if (fieldType == Character.class || fieldType == char.class) {
					return Character.valueOf(value.toString().charAt(0));

				} else {
					return value;
				}
			}
		}
	}

}
