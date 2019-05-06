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
package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart.provided.internal;

import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import com.github.ljtfreitas.restify.http.client.message.converter.form.provided.internal.FormObjectField;
import com.github.ljtfreitas.restify.http.contract.form.FormFieldSerializer;
import com.github.ljtfreitas.restify.http.contract.form.MultipartField;
import com.github.ljtfreitas.restify.http.contract.form.Notation;
import com.github.ljtfreitas.restify.http.contract.form.Form.Field;
import com.github.ljtfreitas.restify.util.Try;

public class MultipartFormObjectField extends FormObjectField {

	private static final String DEFAULT_CONTENT_TYPE = "text/plain";

	private final String contentType;

	MultipartFormObjectField(FormObjectField delegate) {
		this(delegate, DEFAULT_CONTENT_TYPE);
	}

	private MultipartFormObjectField(FormObjectField delegate, String contentType) {
		super(delegate);
		this.contentType = contentType;
	}

	String contentType() {
		return contentType;
	}

	@Override
	protected MultipartFormObjectField using(Notation notation) {
		FormObjectField delegate = super.using(notation);
		return new MultipartFormObjectField(delegate, this.contentType);
	}

	public static MultipartFormObjectField of(java.lang.reflect.Field field, Notation notation) {
		isTrue(field.isAnnotationPresent(MultipartField.class) || field.isAnnotationPresent(Field.class),
				"Your form field must be annotated with @MultipartField or @Field.");

		if (field.isAnnotationPresent(MultipartField.class)) {
			MultipartField annotation = field.getAnnotation(MultipartField.class);

			String name = annotation.value().isEmpty() ? field.getName() : annotation.value();

			boolean indexed = annotation.indexed();

			String contentType = annotation.contentType();

			FormFieldSerializer serializer = Try.of(() -> annotation.serializer().getDeclaredConstructor().newInstance())
					.error(e -> new UnsupportedOperationException("Cannot create a instance of serializer " + annotation.serializer(), e))
						.get();

			return new MultipartFormObjectField(new FormObjectField(name, indexed, serializer, field, notation), contentType);

		} else {
			return new MultipartFormObjectField(FormObjectField.of(field, notation));
		}
	}
}