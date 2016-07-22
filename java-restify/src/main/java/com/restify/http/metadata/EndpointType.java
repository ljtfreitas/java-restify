package com.restify.http.metadata;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

public class EndpointType {

	private final EndpointTarget target;
	private final EndpointMethods endpointMethods;

	public EndpointType(EndpointTarget target, EndpointMethods endpointMethods) {
		this.target = target;
		this.endpointMethods = endpointMethods;
	}

	public Optional<EndpointMethod> find(Method method) {
		return endpointMethods.find(method);
	}

	@Override
	public boolean equals(Object obj) {
		return target.equals(obj);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(target);
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("EndpointType: [")
				.append("Target: ")
					.append(target)
				.append(", ")
				.append("Methods: ")
					.append(endpointMethods)
			.append("]");

		return report.toString();
	}
}
