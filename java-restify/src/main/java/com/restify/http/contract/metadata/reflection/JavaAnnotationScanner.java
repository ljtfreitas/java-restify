package com.restify.http.contract.metadata.reflection;

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
