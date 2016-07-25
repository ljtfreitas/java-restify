package com.restify.http.metadata;

import static com.restify.http.metadata.Preconditions.isTrue;
import static com.restify.http.metadata.Preconditions.nonNull;

import java.util.Objects;
import java.util.Optional;

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
