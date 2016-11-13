package com.restify.http.contract.metadata.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Optional;

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

	public boolean contains(Class<? extends Annotation> javaAnnotationType) {
		return doScan(javaAnnotationType) != null;
	}
}
