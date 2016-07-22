package com.restify.http.metadata;

import java.util.Objects;

public class EndpointHeader {

	private final String name;
	private final String value;

	public EndpointHeader(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String name() {
		return name;
	}

	public String value() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EndpointHeader) {
			EndpointHeader that = (EndpointHeader) obj;

			return this.name.equals(that.name)
				&& this.value.equals(that.value);

		} else return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("EndpointHeader: [")
				.append("Name: ")
					.append(name)
				.append(", ")
				.append("Value: ")
					.append(value)
			.append("]");

		return report.toString();
	}
}
