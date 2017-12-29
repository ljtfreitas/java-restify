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

import java.lang.reflect.Type;

import com.github.ljtfreitas.restify.http.contract.Form;

public class FormObjectParameterSerializer implements ParameterSerializer {

	private final FormObjects formObjects = FormObjects.cache();

	@Override
	public String serialize(String name, Type type, Object source) {
		if (source == null) {
			return null;

		} else if (isFormObject(source)) {
			return doSerialize(source);
		} else {
			throw new IllegalArgumentException("EndpointMethodFormObjectParameterSerializer cannot serialize an object without annotation @Form.");
		}
	}

	private String doSerialize(Object source) {
		FormObject form = formObjects.of(source.getClass());
		return form.serialize(source);
	}

	private boolean isFormObject(Object source) {
		return source.getClass().isAnnotationPresent(Form.class);
	}

}
