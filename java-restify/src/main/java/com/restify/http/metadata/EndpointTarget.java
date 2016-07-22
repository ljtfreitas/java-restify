package com.restify.http.metadata;

import java.util.Objects;
import java.util.Optional;

public class EndpointTarget {

	private final Class<?> type;
	private final String endpoint;

	public EndpointTarget(Class<?> type, String endpoint) {
		this.type = type;
		this.endpoint = endpoint;
	}

	public Class<?> type() {
		return type;
	}

	public Optional<String> endpoint() {
		return Optional.ofNullable(endpoint);
	}

	@Override
	public boolean equals(Object obj) {
		return type.equals(obj);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
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
