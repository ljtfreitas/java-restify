package com.restify.http.contract.metadata.reflection;

import static com.restify.http.util.Preconditions.isTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import com.restify.http.contract.Header;
import com.restify.http.contract.Headers;
import com.restify.http.contract.Path;

public class JavaTypeMetadata {

	private final Class<?> javaType;
	private final Path path;
	private final Header[] headers;
	private final JavaTypeMetadata parent;

	public JavaTypeMetadata(Class<?> javaType) {
		isTrue(javaType.isInterface(), "Your type must be a Java interface.");
		isTrue(javaType.getInterfaces().length <= 1, "Only single inheritance is supported.");

		this.javaType = javaType;
		this.parent = javaType.getInterfaces().length == 1 ? new JavaTypeMetadata(javaType.getInterfaces()[0]) : null;

		this.path = javaType.getAnnotation(Path.class);

		this.headers = Optional.ofNullable(javaType.getAnnotation(Headers.class))
				.map(Headers::value)
					.orElseGet(() -> new JavaAnnotationScanner(javaType).scanAll(Header.class));
	}

	public Optional<Path> path() {
		return Optional.ofNullable(path);
	}

	public Path[] paths() {
		ArrayList<Path> paths = new ArrayList<>();

		Optional.ofNullable(parent)
			.map(p -> p.paths())
				.ifPresent(array -> Collections.addAll(paths, array));

		Optional.ofNullable(path)
			.ifPresent(p -> paths.add(p));

		return paths.toArray(new Path[0]);
	}

	public Header[] headers() {
		ArrayList<Header> headers = new ArrayList<>();

		Optional.ofNullable(parent)
			.map(p -> p.headers())
				.ifPresent(array -> Collections.addAll(headers, array));

		Collections.addAll(headers, this.headers);

		return headers.toArray(new Header[0]);
	}

	public Class<?> javaType() {
		return javaType;
	}

	public Optional<JavaTypeMetadata> parent() {
		return Optional.ofNullable(parent);
	}
}
