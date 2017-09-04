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
package com.github.ljtfreitas.restify.http.contract.metadata.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaAnnotationScanner {

	private final AnnotatedElement javaAnnotatedElement;

	public JavaAnnotationScanner(AnnotatedElement javaAnnotatedElement) {
		this.javaAnnotatedElement = javaAnnotatedElement;
	}

	public <T extends Annotation> Annotation with(Class<T> javaAnnotationType) {
		return Arrays.stream(javaAnnotatedElement.getAnnotations())
				.filter(a -> a.annotationType().isAnnotationPresent(javaAnnotationType))
					.findFirst()
						.orElse(null);
	}

	public <T extends Annotation> Annotation[] allWith(Class<T> javaAnnotationType) {
		return Arrays.stream(javaAnnotatedElement.getAnnotations())
				.filter(a -> a.annotationType().isAnnotationPresent(javaAnnotationType))
					.toArray(Annotation[]::new);
	}

	public <T extends Annotation> T scan(Class<T> javaAnnotationType) {
		return doScan(javaAnnotationType);
	}

	public <T extends Annotation> T[] scanAll(Class<T> javaAnnotationType) {
		return doScanAll(javaAnnotationType);
	}

	private <T extends Annotation> T doScan(Class<T> javaAnnotationType) {
		Optional<T> annotation = Optional.ofNullable(javaAnnotatedElement.getAnnotation(javaAnnotationType));

		return annotation.orElseGet(() -> {
			return Arrays.stream(javaAnnotatedElement.getAnnotations())
					.filter(a -> a.annotationType().isAnnotationPresent(javaAnnotationType))
						.findFirst()
							.map(a -> a.annotationType().getAnnotation(javaAnnotationType))
								.orElse(null);
		});
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> T[] doScanAll(Class<T> javaAnnotationType) {
		T[] array = (T[]) Array.newInstance(javaAnnotationType, 0);

		return Stream.concat(
					Arrays.stream(javaAnnotatedElement.getAnnotationsByType(javaAnnotationType)),
					Arrays.stream(javaAnnotatedElement.getAnnotations())
						.filter(a -> a.annotationType().isAnnotationPresent(javaAnnotationType))
						.map(a -> a.annotationType().getAnnotation(javaAnnotationType)))
				.collect(Collectors.toSet())
					.toArray(array);
	}

	public boolean contains(Class<? extends Annotation> javaAnnotationType) {
		return doScan(javaAnnotationType) != null;
	}
}
