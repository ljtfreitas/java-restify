package com.restify.http.contract.metadata;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class EndpointQueryParameterResolver {

	private final Collection<EndpointMethodParameter> parameters;

	public EndpointQueryParameterResolver(Collection<EndpointMethodParameter> parameters) {
		this.parameters = parameters;
	}

	public String resolve(Object[] args) {
		String query = parameters.stream().filter(p -> p.ofQuery())
			.map(p -> p.resolve(args[p.position()]))
				.filter(p -> !"".equals(p))
					.collect(Collectors.joining("&"));

		return Optional.ofNullable(query)
			.filter(q -> !"".equals(q))
				.map(q -> "?".concat(q))
					.orElse("");
	}
}
