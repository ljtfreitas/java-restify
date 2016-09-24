package com.restify.http.contract.metadata;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class EndpointHeaders {

	private final Collection<EndpointHeader> headers;

	public EndpointHeaders() {
		this.headers = new LinkedHashSet<>();
	}

	public EndpointHeaders(Collection<EndpointHeader> headers) {
		this.headers = new LinkedHashSet<>(headers);
	}

	public void put(EndpointHeader endpointHeader) {
		headers.add(endpointHeader);
	}

	public Collection<EndpointHeader> find(String name) {
		return headers.stream()
			.filter(h -> h.name().equals(name))
				.collect(Collectors.toSet());
	}

	public Optional<EndpointHeader> first(String name) {
		return headers.stream()
				.filter(h -> h.name().equals(name))
					.findFirst();
	}

	public Collection<EndpointHeader> all() {
		return Collections.unmodifiableCollection(headers);
	}

	@Override
	public String toString() {
		return headers.toString();
	}
}
