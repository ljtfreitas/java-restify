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
package com.github.ljtfreitas.restify.http.contract;

import static com.github.ljtfreitas.restify.util.Preconditions.isFalse;
import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.contract.Parameters.Parameter;
import com.github.ljtfreitas.restify.util.Tryable;

public class FormObject {

	private final Class<?> type;
	private final String name;
	private final Map<String, FormObjectField> fields = new LinkedHashMap<>();

	FormObject(Class<?> type) {
		isTrue(type.isAnnotationPresent(Form.class), "Your form class type must be annotated with @Form.");
		this.type = type;
		this.name = Optional.ofNullable(type.getAnnotation(Form.class).value())
				.filter(value -> !value.isEmpty())
					.map(value -> value.endsWith(".") ? value : value + ".")
						.orElse("");
	}

	public Optional<FormObjectField> fieldBy(String name) {
		return Optional.ofNullable(fields.get(name));
	}

	public String serialize(Object source) {
		isTrue(type.isInstance(source), "This FormObject can only serialize objects of class type [" + type + "]");
		return serializeAsParameters(source, name).queryString();
	}

	private Parameters serializeAsParameters(Object source, String name) {
		String prefix = name.isEmpty() || name.endsWith(".") ? name : name + ".";

		return fields.values().stream()
			.reduce(new Parameters(prefix), (parameters, field) -> Tryable.of(() -> parameters.putAll(field.serialize(source))),
					(a, b) -> b);
	}

	void put(String name, boolean indexed, java.lang.reflect.Field field) {
		isFalse(fields.containsKey(name), "Duplicate field [" + name + " on @Form object: " + type);
		fields.put(name, new FormObjectField(name, indexed, field));
	}

	public class FormObjectField {

		private final String name;
		private final boolean indexed;
		private final java.lang.reflect.Field field;

		FormObjectField(String name, boolean indexed, java.lang.reflect.Field field) {
			this.name = name;
			this.indexed = indexed;
			this.field = field;
		}

		private Parameters serialize(Object source) throws Exception {
			field.setAccessible(true);

			Object value = field.get(source);

			return apply(name, value);
		}

		private Parameters apply(String name, Object value) throws Exception {
			if (value != null) {
				if (value.getClass().isAnnotationPresent(Form.class)) {
					return serializeNested(name, value);

				} else if (Iterable.class.isAssignableFrom(value.getClass())) {
					return serializeIterable(name, value);

				} else {
					return Parameters.of(Parameter.of(name, value.toString()));
				}
			} else {
				return Parameters.empty();
			}
		}

		@SuppressWarnings("rawtypes")
		private Parameters serializeIterable(String name, Object value) throws Exception {
			Iterable iterable = (Iterable) value;

			int position = 0;

			Parameters parameters = new Parameters();

			for (Object element : iterable) {
				String newName = indexed ? name + "[" + position + "]" : name;

				parameters = parameters.putAll(apply(newName, element));

				position++;
			}

			return parameters;
		}

		private Parameters serializeNested(String name, Object value) {
			FormObject formObject = FormObjects.cache().of(value.getClass());

			String appendedName = name + (formObject.name.isEmpty() ? "" : "." + formObject.name);

			Parameters nestedParameters = formObject.serializeAsParameters(value, appendedName);

			return nestedParameters;
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