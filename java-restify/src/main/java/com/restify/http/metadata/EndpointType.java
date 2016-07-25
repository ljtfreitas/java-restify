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

	public Class<?> javaType() {
		return target.type();
	}

	public Optional<EndpointMethod> find(Method method) {
		return endpointMethods.find(method);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EndpointType) {
			EndpointType that = (EndpointType) obj;
			return target.equals(that.target);

		} else return false;
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
