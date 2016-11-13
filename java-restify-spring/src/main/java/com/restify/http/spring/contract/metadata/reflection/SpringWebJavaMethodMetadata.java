package com.restify.http.spring.contract.metadata.reflection;

import static com.restify.http.util.Preconditions.isTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.restify.http.contract.metadata.reflection.JavaTypeResolver;

public class SpringWebJavaMethodMetadata {

	private final Method javaMethod;
	private final SpringWebRequestMappingMetadata mapping;

	public SpringWebJavaMethodMetadata(Method javaMethod) {
		this.javaMethod = javaMethod;

		RequestMapping mapping = Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(javaMethod, RequestMapping.class))
				.orElseThrow(() -> new IllegalArgumentException("Method [" + javaMethod + "] does not have a @RequestMapping annotation."));

		isTrue(mapping.value().length <= 1, "Only single path is allowed.");
		isTrue(mapping.method().length == 1, "You must set the HTTP method (only one!) of your Java method [" + javaMethod + "].");

		this.mapping = new SpringWebRequestMappingMetadata(mapping);
	}

	public Optional<String> path() {
		return mapping.path();
	}

	public RequestMethod httpMethod() {
		return mapping.method()[0];
	}

	public Type returnType(Class<?> rawType) {
		return new JavaTypeResolver(rawType).returnTypeOf(javaMethod);
	}

	public String[] headers() {
		return mapping.headers();
	}
}
