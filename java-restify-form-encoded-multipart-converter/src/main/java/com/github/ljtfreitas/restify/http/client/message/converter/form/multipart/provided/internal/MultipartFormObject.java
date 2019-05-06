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

import static com.github.ljtfreitas.restify.util.Preconditions.isFalse;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.ljtfreitas.restify.http.client.message.converter.form.provided.internal.FormObject;
import com.github.ljtfreitas.restify.http.contract.form.MultipartField;
import com.github.ljtfreitas.restify.http.contract.form.Notation;
import com.github.ljtfreitas.restify.http.contract.form.Form.Field;

public class MultipartFormObject extends FormObject {

	private MultipartFormObject(FormObject delegate, Map<String, MultipartFormObjectField> fields) {
		super(delegate.type(), delegate.name(), delegate.notation(), fields);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected MultipartFormObject using(Notation notation) {
		FormObject delegate = super.using(notation);
		return new MultipartFormObject(delegate, (Map<String, MultipartFormObjectField>) delegate.fields());
	}

	public static MultipartFormObject of(Class<?> formObjectType) {
		FormObject delegate = FormObject.of(formObjectType, false);

		Map<String, MultipartFormObjectField> fields = new LinkedHashMap<>();

		Arrays.stream(formObjectType.getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(MultipartField.class) || field.isAnnotationPresent(Field.class))
				.forEach(field -> {
					MultipartFormObjectField formObjectField = MultipartFormObjectField.of(field, delegate.notation());

					isFalse(fields.containsKey(formObjectField.name()),
							"Duplicate field [" + formObjectField.name() + " on @MultipartForm object: " + formObjectType);

					fields.put(formObjectField.name(), formObjectField);
		});

		return new MultipartFormObject(delegate, fields);
	}
}