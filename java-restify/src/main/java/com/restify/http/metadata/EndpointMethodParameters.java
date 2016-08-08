package com.restify.http.metadata;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EndpointMethodParameters {

	private final Map<Integer, EndpointMethodParameter> parameters = new LinkedHashMap<>();

	public Optional<EndpointMethodParameter> get(int position) {
		return Optional.ofNullable(parameters.get(position));
	}

	public Optional<EndpointMethodParameter> find(String name) {
		return parameters.values().stream().filter(p -> p.is(name)).findFirst();
	}

	public Optional<EndpointMethodParameter> ofBody() {
		return parameters.values().stream().filter(p -> p.ofBody()).findFirst();
	}

	public Collection<EndpointMethodParameter> ofQuery() {
		return parameters.values().stream().filter(p -> p.ofQuery())
				.collect(Collectors.toList());
	}

	public void put(EndpointMethodParameter parameter) {
		parameters.put(parameter.position(), parameter);
	}

}
