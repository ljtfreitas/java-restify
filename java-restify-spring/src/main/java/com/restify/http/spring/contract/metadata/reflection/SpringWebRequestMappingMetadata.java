package com.restify.http.spring.contract.metadata.reflection;

import static com.restify.http.util.Preconditions.nonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class SpringWebRequestMappingMetadata {

	private final RequestMapping mapping;

	public SpringWebRequestMappingMetadata(RequestMapping mapping) {
		this.mapping = nonNull(mapping, "@RequestMapping cannot be null");
	}

	public Optional<String> path() {
		return Optional.ofNullable(mapping)
				.map(m -> m.value())
					.filter(m -> m.length == 1)
						.map(m -> m[0])
							.filter(p -> !p.isEmpty());
	}

	public RequestMethod[] method() {
		return mapping.method();
	}

	public String[] headers() {
		ArrayList<String> headers = new ArrayList<>();

		Collections.addAll(headers, mappedHeaders());

		consumes().ifPresent(c -> headers.add(c));
		produces().ifPresent(p -> headers.add(p));

		return headers.toArray(new String[0]);
	}

	private String[] mappedHeaders() {
		return Optional.ofNullable(mapping)
				.map(m -> m.headers())
					.orElseGet(() -> new String[0]);
	}

	private Optional<String> consumes() {
		return Optional.ofNullable(mapping)
			.map(m -> m.consumes())
				.filter(c -> c.length >= 1)
					.map(h -> Arrays.stream(h)
							.filter(c -> c != null && !c.isEmpty())
								.collect(Collectors.joining(", ")))
				.map(c -> "Accept=".concat(c));
	}

	private Optional<String> produces() {
		String[] produces = Optional.ofNullable(mapping)
			.map(m -> m.produces())
				.filter(p -> p.length <= 1)
					.orElseThrow(() -> new IllegalArgumentException("[produces] parameter (of @RequestMapping annotation) "
							+ "must have only single value."));

		return (produces.length == 0) ? Optional.empty()
				: Optional.ofNullable(produces[0])
					.filter(p -> !p.isEmpty())
						.map(p -> "Content-Type=".concat(p));
	}

	@Override
	public String toString() {
		return mapping.toString();
	}
}
