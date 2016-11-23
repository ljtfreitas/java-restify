/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.contract.metadata;

import static com.github.ljtfreitas.restify.http.util.Preconditions.isFalse;
import static com.github.ljtfreitas.restify.http.util.Preconditions.isTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.github.ljtfreitas.restify.http.contract.Form;
import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.http.contract.Form.Field;

public class FormObjects {

	private static final FormObjects singleton = new FormObjects();

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

	public static FormObjects cache() {
		return singleton;
	}

	public static class FormObject {

		private final Class<?> type;
		private final Map<String, FormObjectField> fields = new LinkedHashMap<>();

		private FormObject(Class<?> type) {
			isTrue(type.isAnnotationPresent(Form.class), "Your form class type must be annotated with @Form.");
			this.type = type;
		}

		public Optional<FormObjectField> fieldBy(String name) {
			return Optional.ofNullable(fields.get(name));
		}

		public String serialize(Object source) {
			Parameters parameters = new Parameters();

			fields.values().forEach(field -> {
				try {
					field.applyOn(parameters, source);
				} catch (Exception e) {
					throw new UnsupportedOperationException(e);
				}
			});

			return parameters.queryString();
		}

		private void put(String name, java.lang.reflect.Field field) {
			isFalse(fields.containsKey(name), "Duplicate field [" + name + " on @Form object: " + type);
			fields.put(name, new FormObjectField(name, field));
		}

		public class FormObjectField {

			private final String name;
			private final java.lang.reflect.Field field;

			FormObjectField(String name, java.lang.reflect.Field field) {
				this.name = name;
				this.field = field;
			}

			@SuppressWarnings("rawtypes")
			public void applyOn(Parameters parameters, Object source) throws Exception {
				field.setAccessible(true);

				if (Iterable.class.isAssignableFrom(field.getType())) {
					Iterable iterable = (Iterable) field.get(source);

					for (Object element : iterable) {
						parameters.put(name, element.toString());
					}
				} else {
					parameters.put(name, field.get(source).toString());
				}
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
