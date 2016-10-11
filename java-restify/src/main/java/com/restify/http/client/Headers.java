package com.restify.http.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

public class Headers {

	public static final String ACCEPT = "Accept";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CONTENT_LENGTH = "Content-Length";

	private final Collection<Header> headers;

	public Headers() {
		this.headers = new LinkedHashSet<>();
	}

	public Headers(Header... headers) {
		this.headers = new LinkedHashSet<>(Arrays.asList(headers));
	}

	public Headers(Headers source) {
		this.headers = new LinkedHashSet<>(source.headers);
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

	public void replace(String name, String value) {
		headers.removeIf(h -> h.name().equalsIgnoreCase(name));
		headers.add(new Header(name, value));
	}

	public Collection<Header> all() {
		return Collections.unmodifiableCollection(headers);
	}

	public Optional<Header> get(String name) {
		return headers.stream().filter(h -> h.name().equalsIgnoreCase(name))
				.findFirst();
	}

	@Override
	public String toString() {
		return headers.toString();
	}
}
