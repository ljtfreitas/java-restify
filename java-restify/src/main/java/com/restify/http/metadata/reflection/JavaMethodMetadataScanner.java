package com.restify.http.metadata.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class JavaMethodMetadataScanner {

	private final java.lang.reflect.Method javaMethod;

	public JavaMethodMetadataScanner(Method javaMethod) {
		this.javaMethod = javaMethod;
	}

	public <T extends Annotation> T scan(Class<T> javaAnnotationType) {
		Optional<T> annotation = Optional.ofNullable(javaMethod.getAnnotation(javaAnnotationType));

		return annotation.orElseGet(() -> {
			return Arrays.stream(javaMethod.getAnnotations())
					.filter(a -> a.annotationType().isAnnotationPresent(javaAnnotationType))
						.findFirst()
							.map(a -> a.annotationType().getAnnotation(javaAnnotationType))
								.orElse(null);
		});
	}
}
