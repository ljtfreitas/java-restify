package com.restify.http.contract;

import static com.restify.http.util.Preconditions.isTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContentType {

	private final String type;
	private final ContentTypeParameters parameters;

	private ContentType(String type, ContentTypeParameters parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public String name() {
		return type;
	}

	public String value() {
		return type;
	}

	public Optional<String> parameter(String name) {
		return parameters.get(name);
	}

	public ContentType newParameter(String name, String value) {
		ContentTypeParameters newParameters = parameters.put(name, value);
		return new ContentType(type, newParameters);
	}

	public ContentTypeParameters parameters() {
		return parameters;
	}

	public boolean is(String contentType) {
		return this.type.equals(contentType) || this.type.startsWith(contentType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ContentType) {
			ContentType that = (ContentType) obj;
			return this.type.equals(that.type) || this.type.startsWith(that.type);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(type);

		if (!parameters.empty()) {
			sb.append("; ").append(parameters.toString());
		}

		return sb.toString();
	}

	public static ContentType of(String type) {
		String[] parts = type.split(";");

		isTrue(parts.length >= 1, "Your Content-Type source is invalid: " + type);

		String[] parameters = Arrays.copyOfRange(parts, 1, parts.length);

		return new ContentType(parts[0], ContentTypeParameters.of(parameters));
	}

	public static class ContentTypeParameters {

		private final Map<String, String> parameters;

		private ContentTypeParameters(Map<String, String> parameters) {
			this.parameters = new LinkedHashMap<>(parameters);
		}

		private Optional<String> get(String name) {
			return Optional.ofNullable(parameters.get(name));
		}

		private ContentTypeParameters put(String name, String value) {
			ContentTypeParameters newParameters = new ContentTypeParameters(parameters);
			newParameters.parameters.put(name, value);
			return newParameters;
		}

		public boolean empty() {
			return parameters.isEmpty();
		}

		@Override
		public String toString() {
			return parameters.entrySet().stream()
					.map(p -> p.getKey() + "=" + p.getValue())
						.collect(Collectors.joining("; "));
		}

		private static ContentTypeParameters of(String[] parameters) {
			Map<String, String> mapOfParameters = new LinkedHashMap<>();

			Arrays.stream(parameters).map(p -> p.split("=")).filter(p -> p.length == 2)
					.forEach(p -> mapOfParameters.put(p[0].trim(), p[1].trim()));

			return new ContentTypeParameters(mapOfParameters);
		}
	}
}