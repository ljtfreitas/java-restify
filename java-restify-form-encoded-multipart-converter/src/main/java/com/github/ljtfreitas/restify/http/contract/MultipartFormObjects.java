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

import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.github.ljtfreitas.restify.http.contract.MultipartForm;
import com.github.ljtfreitas.restify.http.contract.Form.Field;
import com.github.ljtfreitas.restify.http.contract.MultipartForm.MultipartField;

public class MultipartFormObjects {

	private static final MultipartFormObjects singleton = new MultipartFormObjects();

	private final Map<Class<?>, MultipartFormObject> cache = new ConcurrentHashMap<>();

	private MultipartFormObjects() {
	}

	public MultipartFormObject of(Class<?> formObjectType) {
		return get(formObjectType).orElseGet(() -> create(formObjectType));
	}

	private MultipartFormObject create(Class<?> formObjectType) {
		MultipartFormObject formObjectMetadata = new MultipartFormObject(formObjectType);

		Arrays.stream(formObjectType.getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(Field.class) || f.isAnnotationPresent(MultipartField.class))
			.forEach(f -> {
				String name = Optional.ofNullable(f.getAnnotation(MultipartField.class))
						.map(MultipartField::value).filter(n -> n != null && !"".equals(n))
							.orElseGet(() -> Optional.ofNullable(f.getAnnotation(Field.class))
									.map(Field::value).filter(n -> n != null && !"".equals(n))
										.orElse(f.getName()));

				formObjectMetadata.put(name, f);
		});

		cache.put(formObjectType, formObjectMetadata);

		return formObjectMetadata;
	}

	private Optional<MultipartFormObject> get(Class<?> formObjectType) {
		return Optional.ofNullable(cache.get(formObjectType));
	}

	public static MultipartFormObjects cache() {
		return singleton;
	}

	public static class MultipartFormObject {

		private final Class<?> type;
		private final Collection<FormObjectField> fields = new LinkedHashSet<>();

		private MultipartFormObject(Class<?> type) {
			isTrue(type.isAnnotationPresent(MultipartForm.class), "Your form class type must be annotated with @MultipartForm.");
			this.type = type;
		}

		public Collection<FormObjectField> fields() {
			return Collections.unmodifiableCollection(fields);
		}

		private void put(String name, java.lang.reflect.Field field) {
			isTrue(fields.add(new FormObjectField(name, field)), "Duplicate field [" + name + " on @MultipartForm object: " + type);
		}

		public class FormObjectField {

			private final String name;
			private final java.lang.reflect.Field field;

			FormObjectField(String name, java.lang.reflect.Field field) {
				this.name = name;
				this.field = field;
			}

			public String name() {
				return name;
			}

			public Object valueOn(Object source) {
				try {
					field.setAccessible(true);
					return field.get(source);

				} catch (Exception e) {
					throw new UnsupportedOperationException(e);
				}
			}

			@Override
			public int hashCode() {
				return Objects.hash(name, field);
			}

			@Override
			public boolean equals(Object obj) {
				if (obj instanceof FormObjectField) {
					FormObjectField that = (FormObjectField) obj;

					return Objects.equals(name, that.name)
						&& Objects.equals(field, that.field);

				} else {
					return false;
				}
			}
		}
	}

}
