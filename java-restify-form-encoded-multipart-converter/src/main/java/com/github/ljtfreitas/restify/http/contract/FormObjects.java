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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
			.filter(field -> field.isAnnotationPresent(Field.class))
			.forEach(field -> {
				Field annotation = field.getAnnotation(Field.class);

				String name = annotation.value().isEmpty() ? field.getName() : annotation.value();
				boolean indexed = annotation.indexed();

				formObjectMetadata.put(name, indexed, field);
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

}
