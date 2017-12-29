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
package com.github.ljtfreitas.restify.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaAnnotationScanner {

	private final AnnotatedElement javaAnnotatedElement;

	public JavaAnnotationScanner(AnnotatedElement javaAnnotatedElement) {
		this.javaAnnotatedElement = javaAnnotatedElement;
	}

	public <T extends Annotation> Optional<Annotation> with(Class<T> javaAnnotationType) {
		return doWith(javaAnnotationType).findFirst();
	}

	public <T extends Annotation> Collection<Annotation> allWith(Class<T> javaAnnotationType) {
		return doWith(javaAnnotationType).collect(Collectors.toList());
	}

	private <T extends Annotation> Stream<Annotation> doWith(Class<T> javaAnnotationType) {
		return Arrays.stream(javaAnnotatedElement.getAnnotations())
				.filter(a -> a.annotationType().isAnnotationPresent(javaAnnotationType));
	}

	public <T extends Annotation> Optional<T> scan(Class<T> javaAnnotationType) {
		return doScan(javaAnnotationType);
	}

	public <T extends Annotation> Collection<T> scanAll(Class<T> javaAnnotationType) {
		return doScanAll(javaAnnotationType);
	}

	private <T extends Annotation> Optional<T> doScan(Class<T> javaAnnotationType) {
		Optional<T> annotation = Optional.ofNullable(javaAnnotatedElement.getAnnotation(javaAnnotationType));

		return Optional.ofNullable(annotation.orElseGet(() -> {
			return doWith(javaAnnotationType)
					.findFirst()
						.map(a -> a.annotationType().getAnnotation(javaAnnotationType))
							.orElse(null);
		}));
	}

	private <T extends Annotation> Collection<T> doScanAll(Class<T> javaAnnotationType) {
		return Stream.concat(
					Arrays.stream(javaAnnotatedElement.getAnnotationsByType(javaAnnotationType)),
					Arrays.stream(javaAnnotatedElement.getAnnotations())
						.filter(a -> a.annotationType().isAnnotationPresent(javaAnnotationType))
						.map(a -> a.annotationType().getAnnotation(javaAnnotationType)))
				.collect(Collectors.toList());
	}

	public boolean contains(Class<? extends Annotation> javaAnnotationType) {
		return doScan(javaAnnotationType).isPresent();
	}
}
