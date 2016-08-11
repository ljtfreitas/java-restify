package com.restify.http.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

public class Headers {

	private final Collection<Header> headers;

	public Headers() {
		this.headers = new LinkedHashSet<>();
	}

	public Headers(Header... headers) {
		this.headers = new LinkedHashSet<>(Arrays.asList(headers));
	}

	public void add(Header header) {
		headers.add(header);
	}

	public void put(String name, String value) {
		headers.add(new Header(name, value));
	}

	public void put(String name, Collection<String> values) {
		values.forEach(value -> headers.add(new Header(name, value)));
	}

	public Collection<Header> all() {
		return Collections.unmodifiableCollection(headers);
	}

	public Optional<Header> get(String name) {
		return headers.stream().filter(h -> h.name().equals(name))
				.findFirst();
	}

	@Override
	public String toString() {
		return headers.toString();
	}
}
