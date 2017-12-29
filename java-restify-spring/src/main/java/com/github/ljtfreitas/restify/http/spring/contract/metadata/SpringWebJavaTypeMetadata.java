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
package com.github.ljtfreitas.restify.http.spring.contract.metadata;

import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

class SpringWebJavaTypeMetadata {

	private final Class<?> javaType;
	private final Optional<SpringWebRequestMappingMetadata> mapping;
	private final SpringWebJavaTypeMetadata parent;

	public SpringWebJavaTypeMetadata(Class<?> javaType) {
		isTrue(javaType.isInterface(), "Your type must be a Java interface.");
		isTrue(javaType.getInterfaces().length <= 1, "Only single inheritance is supported.");

		RequestMapping mapping = AnnotatedElementUtils.getMergedAnnotation(javaType, RequestMapping.class);
		if (mapping != null) {
			isTrue(mapping.value().length <= 1, "Only single path is allowed.");
			isTrue(mapping.method().length ==  0, "You must not set the HTTP method at the class level, only at the method level.");
		}

		this.mapping = Optional.ofNullable(mapping).map(m -> new SpringWebRequestMappingMetadata(m));
		this.javaType = javaType;
		this.parent = javaType.getInterfaces().length == 1 ? new SpringWebJavaTypeMetadata(javaType.getInterfaces()[0]) : null;
	}

	public Class<?> javaType() {
		return javaType;
	}

	public Optional<String> path() {
		return mapping.flatMap(m -> m.path());
	}

	public String[] paths() {
		ArrayList<String> paths = new ArrayList<>();

		Optional.ofNullable(parent)
			.map(p -> p.paths())
				.ifPresent(array -> Collections.addAll(paths, array));

		mapping.flatMap(m -> m.path())
			.ifPresent(p -> paths.add(p));

		return paths.toArray(new String[0]);
	}

	public String[] headers() {
		return mapping.map(m -> m.headers()).orElseGet(() -> new String[0]);
	}
}
