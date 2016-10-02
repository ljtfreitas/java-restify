package com.restify.http.spring.autoconfigure;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

class RestifyableType {

	private final Class<?> objectType;
	private final Restifyable restifyable;

	RestifyableType(String objectTypeName) {
		try {
			this.objectType = Class.forName(objectTypeName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("@Restifyable type not found on classpath: [" + objectTypeName + "]", e);
		}
		this.restifyable = findAnnotation(this.objectType, Restifyable.class);
	}

	public Class<?> objectType() {
		return objectType;
	}

	public String name() {
		return restifyable.name();
	}

	public String description() {
		return restifyable.description();
	}

	public Optional<URL> endpoint() {
		return Optional.ofNullable(restifyable.endpoint()).filter(endpoint -> !endpoint.isEmpty()).map(this::convert);
	}

	private URL convert(String endpoint) {
		try {
			return new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new RuntimeException(
					"Malformed URL on @Restifyable API [" + restifyable.name() + "] " + "configuration: " + endpoint, e);
		}
	}
}
