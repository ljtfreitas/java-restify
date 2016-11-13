package com.restify.http.contract.metadata;

import static com.restify.http.util.Preconditions.isFalse;

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
		return parameters.values().stream().filter(p -> p.body()).findFirst();
	}

	public Collection<EndpointMethodParameter> ofQuery() {
		return parameters.values().stream().filter(p -> p.query())
				.collect(Collectors.toList());
	}

	public Collection<EndpointMethodParameter> callbacks() {
		return parameters.values().stream().filter(p -> p.callback())
				.collect(Collectors.toList());
	}

	public Collection<EndpointMethodParameter> callbacks(Class<?> callbackClassType) {
		return parameters.values().stream()
				.filter(p -> p.callback() && callbackClassType.isAssignableFrom(p.javaType().classType()))
					.collect(Collectors.toList());
	}

	public void put(EndpointMethodParameter parameter) {
		if (parameter.body()) {
			isFalse(parameters.values().stream().anyMatch(EndpointMethodParameter::body), "Only one parameter annotated with @BodyParameter is allowed.");
		}

		parameters.put(parameter.position(), parameter);
	}
}
