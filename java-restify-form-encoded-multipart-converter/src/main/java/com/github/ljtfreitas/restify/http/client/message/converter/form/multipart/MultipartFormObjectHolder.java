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
package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.contract.Form;
import com.github.ljtfreitas.restify.http.contract.MultipartForm;
import com.github.ljtfreitas.restify.http.contract.MultipartFormObject;
import com.github.ljtfreitas.restify.http.contract.MultipartFormObject.MultipartFormObjectField;
import com.github.ljtfreitas.restify.http.contract.MultipartFormObjects;

class MultipartFormObjectHolder {

	private final MultipartFormObject formObject;
	private final Object form;

	MultipartFormObjectHolder(MultipartFormObject formObject, Object form) {
		this.formObject = formObject;
		this.form = form;
	}

	Collection<MultipartFormObjectFieldHolder> fields() {
		return collect(formObject, formObject.name(), form);
	}

	private Collection<MultipartFormObjectFieldHolder> collect(MultipartFormObject multipartFormObject, String prefix, Object form) {
		return multipartFormObject.fields().values().stream()
				.flatMap(field -> deep(field, field.prefixedBy(prefix).name(), field.valueOn(form)).stream())
					.collect(Collectors.toList());
	}

	private Collection<MultipartFormObjectFieldHolder> deep(MultipartFormObjectField field, String name, Object value) {
		if (value == null) {
			return Collections.emptyList();

		} else {
			if (value.getClass().isAnnotationPresent(Form.class) || value.getClass().isAnnotationPresent(MultipartForm.class)) {
				return nested(field, name, value);

			} else if (Iterable.class.isAssignableFrom(value.getClass())) {
				return iterable(field, name, value);

			} else {
				return Collections.singleton(new MultipartFormObjectFieldHolder(name, value, field.contentType()));
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private Collection<MultipartFormObjectFieldHolder> iterable(MultipartFormObjectField field, String name, Object value) {
		Iterable iterable = (Iterable) value;

		int position = 0;

		boolean indexed = field.indexed();

		Collection<MultipartFormObjectFieldHolder> elements = new LinkedList<>();

		for (Object element : iterable) {
			String newName = indexed ? name + "[" + position + "]" : name;

			elements.addAll(deep(field, newName, element));

			position++;
		}

		return elements;
	}

	private Collection<MultipartFormObjectFieldHolder> nested(MultipartFormObjectField field, String name, Object value) {
		MultipartFormObject nested = MultipartFormObjects.cache().of(value.getClass());

		String newPrefix = name + (nested.name().isEmpty() ? "" : "." + nested.name());

		return collect(nested, newPrefix, value);
	}

	class MultipartFormObjectFieldHolder {

		private final String name;
		private final Object value;
		private final String contentType;

		private MultipartFormObjectFieldHolder(String name, Object value, String contentType) {
			this.name = name;
			this.value = value;
			this.contentType = contentType;
		}

		String name() {
			return name;
		}

		Object value() {
			return value;
		}

		Class<?> type() {
			return value.getClass();
		}

		public String contentType() {
			return contentType;
		}
	}
}
