package com.restify.http.metadata.reflection;

import java.util.Optional;

import com.restify.http.contract.Header;
import com.restify.http.contract.Headers;
import com.restify.http.contract.Path;

public class JavaTypeMetadata {

	private final Class<?> javaType;
	private final Path path;
	private final Header[] headers;

	public JavaTypeMetadata(Class<?> javaType) {
		this.javaType = javaType;
		this.path = javaType.getAnnotation(Path.class);

		this.headers = Optional.ofNullable(javaType.getAnnotation(Headers.class))
						.map(Headers::value)
							.orElseGet(() -> javaType.getAnnotationsByType(Header.class));
	}

	public Optional<Path> path() {
		return Optional.ofNullable(path);
	}

	public Header[] headers() {
		return headers;
	}

	public Class<?> javaType() {
		return javaType;
	}

}
