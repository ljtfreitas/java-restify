package com.restify.http.contract;

import static com.restify.http.util.Preconditions.nonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Cookies {

	private final Map<String, Cookie> cookies = new HashMap<>();

	public Cookies add(String name, String value) {
		nonNull(name, "Cookie name can't be null");
		nonNull(value, "Cookie value can't be null");
		cookies.put(name, new Cookie(name, value));
		return this;
	}

	@Override
	public String toString() {
		String content = cookies.values().stream()
			.map(c -> c.name + "=" + c.value)
				.collect(Collectors.joining("; "));
		return content;
	}

	private class Cookie {

		private final String name;
		private final String value;

		public Cookie(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
}
