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
package com.github.ljtfreitas.restify.http.contract.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.reflection.JavaAnnotationScanner;

public class EndpointMethodMetadata {

	private final Map<Class<? extends Annotation>, List<Annotation>> annotations;

	public EndpointMethodMetadata(Method javaMethod) {
		this.annotations = scan(javaMethod).stream().collect(Collectors.groupingBy(Annotation::annotationType));
	}

	private List<Annotation> scan(Method javaMethod) {
		List<Annotation> annotations = new ArrayList<>();
		annotations.addAll(new JavaAnnotationScanner(javaMethod).allWith(Metadata.class));
		annotations.addAll(new JavaAnnotationScanner(javaMethod.getDeclaringClass()).allWith(Metadata.class));
		return annotations;
	}

	public <A extends Annotation> boolean contains(Class<A> annotation) {
		return annotations.containsKey(annotation);
	}

	@SuppressWarnings("unchecked")
	public <A extends Annotation> Optional<A> get(Class<A> annotation) {
		return annotations.getOrDefault(annotation, Collections.emptyList()).stream()
				.findFirst()
					.map(a -> (A) a);
	}

	@SuppressWarnings("unchecked")
	public <A extends Annotation> Collection<A> all(Class<A> annotation) {
		return (List<A>) annotations.getOrDefault(annotation, Collections.emptyList());
	}

	public Collection<Annotation> all() {
		return annotations.values().stream()
				.flatMap(c -> c.stream())
					.collect(Collectors.toList());
	}

	public static EndpointMethodMetadata of(Method method) {
		return new EndpointMethodMetadata(method);
	}
}
