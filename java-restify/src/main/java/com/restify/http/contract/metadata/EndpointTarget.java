package com.restify.http.contract.metadata;

import static com.restify.http.util.Preconditions.isTrue;
import static com.restify.http.util.Preconditions.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class EndpointTarget {

	private final Class<?> type;
	private final String endpoint;

	public EndpointTarget(Class<?> type) {
		this(type, null);
	}

	public EndpointTarget(Class<?> type, String endpoint) {
		nonNull(type, "EndpointTarget type cannot be null.");
		isTrue(type.isInterface(), "EndpointTarget type must be a interface.");

		this.type = type;
		this.endpoint = endpoint;
	}

	public Class<?> type() {
		return type;
	}

	public Optional<String> endpoint() {
		return Optional.ofNullable(endpoint);
	}

	public Collection<Method> methods() {
		return Arrays.stream(type.getMethods())
				.filter(javaMethod -> javaMethod.getDeclaringClass() != Object.class
					|| !javaMethod.isDefault()
					|| !Modifier.isStatic(javaMethod.getModifiers()))
				.collect(Collectors.toSet());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EndpointTarget) {
			EndpointTarget that = (EndpointTarget) obj;
			return type.equals(that.type) && Objects.equals(endpoint, that.endpoint);

		} else return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, endpoint);
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("EndpointTarget: [")
				.append("Java Type: ")
					.append(type)
				.append(", ")
				.append("Endpoint: ")
					.append(endpoint == null ? "(empty)" : endpoint)
			.append("]");

		return report.toString();
	}
}
