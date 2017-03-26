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
package com.github.ljtfreitas.restify.spring.configure;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.beans.Introspector;
import java.util.Optional;

class RestifyableType {

	private final Class<?> objectType;
	private final Restifyable restifyable;

	RestifyableType(String objectTypeName) {
		try {
			this.objectType = Class.forName(objectTypeName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("@Restifyable type not found on classpath: [" + objectTypeName + "]", e);
		}
		this.restifyable = findAnnotation(this.objectType, Restifyable.class);
	}

	public Class<?> objectType() {
		return objectType;
	}

	public String name() {
		return doName();
	}

	private String doName() {
		return Optional.ofNullable(restifyable.name())
				.filter(n -> !n.isEmpty())
					.orElseGet(() -> Introspector.decapitalize(objectType.getSimpleName()));
	}

	public String description() {
		return restifyable.description();
	}

	public Optional<String> endpoint() {
		return Optional.ofNullable(restifyable.endpoint()).filter(endpoint -> !endpoint.isEmpty());
	}
}
