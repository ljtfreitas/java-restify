package com.restify.http.metadata;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

public class EndpointHeaders {

	private final Collection<EndpointHeader> headers;

	public EndpointHeaders() {
		this.headers = new LinkedHashSet<>();
	}

	public EndpointHeaders(Collection<EndpointHeader> headers) {
		this.headers = new LinkedHashSet<>(headers);
	}

	public Collection<EndpointHeader> all() {
		return Collections.unmodifiableCollection(headers);
	}

	@Override
	public String toString() {
		return headers.toString();
	}
}
