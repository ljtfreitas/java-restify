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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.contract.Form.Field;
import com.github.ljtfreitas.restify.http.contract.FormObject.FormObjectField;
import com.github.ljtfreitas.restify.http.contract.MultipartForm.MultipartField;
import com.github.ljtfreitas.restify.util.Try;

public class MultipartFormObject {

	private final String name;
	private final Class<?> type;
	private final Map<String, MultipartFormObjectField> fields;

	public MultipartFormObject(Class<?> type, String name, Map<String, MultipartFormObjectField> fields) {
		this.type = type;
		this.name = name;
		this.fields = fields;
	}

	public Class<?> type() {
		return type;
	}

	public String name() {
		return name;
	}

	public Map<String, MultipartFormObjectField> fields() {
		return fields.values().stream()
				.collect(Collectors.toMap(MultipartFormObjectField::name, Function.identity(), (a, b) -> b, LinkedHashMap::new));
	}

	public static MultipartFormObject of(Class<?> formObjectType) {
		isTrue(formObjectType.isAnnotationPresent(MultipartForm.class) || formObjectType.isAnnotationPresent(Form.class),
				"Your form class type must be annotated with @MultipartForm or @Form.");

		String name = Optional.ofNullable(formObjectType.getAnnotation(MultipartForm.class))
				.map(MultipartForm::value)
					.orElseGet(() -> Optional.ofNullable(formObjectType.getAnnotation(Form.class))
						.map(Form::value).orElse(""));

		Map<String, MultipartFormObjectField> fields = new LinkedHashMap<>();

		Arrays.stream(formObjectType.getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(Field.class) || field.isAnnotationPresent(MultipartField.class))
				.forEach(field -> {
					MultipartFormObjectField formObjectField = MultipartFormObjectField.of(field);

					isFalse(fields.containsKey(formObjectField.name),
							"Duplicate field [" + name + " on @MultipartForm object: " + formObjectType);

					fields.put(formObjectField.name, formObjectField);
		});

		return new MultipartFormObject(formObjectType, name, fields);
	}

	public static class MultipartFormObjectField {

		private final String name;
		private final boolean indexed;
		private final String contentType;
		private final FormFieldSerializer serializer;
		private final java.lang.reflect.Field field;

		MultipartFormObjectField(String name, boolean indexed, String contentType, FormFieldSerializer serializer,
				java.lang.reflect.Field field) {
			this.name = name;
			this.indexed = indexed;
			this.contentType = contentType;
			this.serializer = serializer;
			this.field = field;
		}

		public MultipartFormObjectField prefixedBy(String prefix) {
			String newName = (prefix.isEmpty() || prefix.endsWith(".") ? prefix : prefix + ".")
					+ name;

			return new MultipartFormObjectField(newName, indexed, contentType, serializer, field);
		}

		public String name() {
			return name;
		}

		public boolean indexed() {
			return indexed;
		}

		public Class<?> type() {
			return field.getType();
		}

		public String contentType() {
			return contentType;
		}

		public Object valueOn(Object instance) {
			return Try.of(() -> doValueOn(instance)).error(UnsupportedOperationException::new).get();
		}

		public Object doValueOn(Object instance) throws IllegalArgumentException, IllegalAccessException {
			field.setAccessible(true);
			Object value = field.get(instance);
			return value == null ? null : serializer.serialize(value.getClass(), value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, field);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof MultipartFormObjectField) {
				MultipartFormObjectField that = (MultipartFormObjectField) obj;

				return Objects.equals(name, that.name)
					&& Objects.equals(field, that.field);

			} else {
				return false;
			}
		}

		public static MultipartFormObjectField of(java.lang.reflect.Field field) {
			isTrue(field.isAnnotationPresent(MultipartField.class) || field.isAnnotationPresent(Field.class),
					"Your form field must be annotated with @MultipartField or @Field.");

			if (field.isAnnotationPresent(MultipartField.class)) {
				MultipartField annotation = field.getAnnotation(MultipartField.class);

				String name = Optional.ofNullable(annotation.value())
						.filter(n-> !n.trim().isEmpty())
							.orElseGet(field::getName);

				boolean indexed = annotation.indexed();

				String contentType = annotation.contentType();

				FormFieldSerializer serializer = Try.of(annotation.serializer()::newInstance)
						.error(e -> new UnsupportedOperationException("Cannot create a instance of serializer " + annotation.serializer(), e))
							.get();

				return new MultipartFormObjectField(name, indexed, contentType, serializer, field);

			} else {
				FormObjectField delegate = FormObjectField.of(field);

				return new MultipartFormObjectField(delegate.name(), delegate.indexed(), "text/plain", delegate.serializer(), field);
			}
		}
	}
}