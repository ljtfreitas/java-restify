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
package com.github.ljtfreitas.restify.http.client.message.converter.form;

import java.lang.reflect.Type;
import java.util.Arrays;

import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.contract.Form;
import com.github.ljtfreitas.restify.http.contract.FormObject;
import com.github.ljtfreitas.restify.http.contract.FormObjectParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.FormObjects;
import com.github.ljtfreitas.restify.reflection.JavaAnnotationScanner;

public class FormURLEncodedFormObjectMessageConverter extends BaseFormURLEncodedMessageConverter<Object> {

	private final FormObjects formObjects = FormObjects.cache();

	public FormURLEncodedFormObjectMessageConverter() {
		super(new FormObjectParameterSerializer());
	}
	
	@Override
	public boolean canRead(Type type) {
		return type instanceof Class && ((Class<?>) type).isAnnotationPresent(Form.class);
	}

	@Override
	protected Object doRead(Type expectedType, ParameterPair[] pairs) {
		Class<?> type = (Class<?>) expectedType;

		try {
			FormObject formObject = formObjects.of(type);

			Object result = type.newInstance();

			Arrays.stream(pairs)
				.forEach(p -> formObject.fieldBy(p.name()).ifPresent(f -> f.applyTo(result, p.value())));

			return result;

		} catch (InstantiationException | IllegalAccessException e) {
			throw new HttpMessageReadException(e);
		}
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return new JavaAnnotationScanner(type).contains(Form.class);
	}
}
