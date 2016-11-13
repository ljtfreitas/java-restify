package com.restify.http.spring.contract.metadata.reflection;

import static com.restify.http.util.Preconditions.isTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

public class SpringWebJavaTypeMetadata {

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
