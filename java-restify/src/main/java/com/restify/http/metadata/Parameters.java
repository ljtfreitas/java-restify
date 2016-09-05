package com.restify.http.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.restify.http.client.charset.Encoding;

public class Parameters {

	private Map<String, List<String>> parameters = new LinkedHashMap<>();

	public void put(String name, String value) {
		parameters.compute(name, (k, v) -> Optional.ofNullable(v).orElseGet(() -> new ArrayList<>()))
			.add(value);
	}

	public Optional<String> first(String name) {
		return Optional.ofNullable(parameters.get(name))
			.flatMap(values -> values.stream().findFirst());
	}

	public Collection<Parameter> all() {
		return parameters.entrySet().stream()
					.map(e -> new Parameter(e.getKey(), e.getValue()))
						.collect(Collectors.toList());
	}

	public String queryString() {
		StringJoiner joiner = new StringJoiner("&");

		parameters.forEach((name, values) -> {
			values.forEach(v -> joiner.add(encode(name) + "=" + encode(v)));
		});

		return joiner.toString();
	}

	private String encode(String value) {
		return Encoding.UTF_8.encode(value);
	}

	public class Parameter {

		private final String name;
		private final Collection<String> values;

		public Parameter(String name, Collection<String> values) {
			this.name = name;
			this.values = values;
		}

		public String name() {
			return name;
		}

		public Collection<String> values() {
			return values;
		}

	}
}
