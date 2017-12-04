/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.spring.contract.metadata;

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

class SpringWebRequestMappingMetadata {

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

	private Optional<String> produces() {
		return Optional.ofNullable(mapping)
			.map(m -> m.produces())
				.filter(c -> c.length >= 1)
					.map(h -> Arrays.stream(h)
							.filter(c -> c != null && !c.isEmpty())
								.collect(Collectors.joining(", ")))
				.map(c -> HttpHeaders.ACCEPT + "=" + c);
	}

	private Optional<String> consumes() {
		String[] consumes = Optional.ofNullable(mapping)
			.map(m -> m.consumes())
				.filter(p -> p.length <= 1)
					.orElseThrow(() -> new IllegalArgumentException("[consumes] parameter (of @RequestMapping annotation) "
							+ "must have only single value."));

		return (consumes.length == 0) ? Optional.empty()
				: Optional.ofNullable(consumes[0])
					.filter(p -> !p.isEmpty())
						.map(p -> HttpHeaders.CONTENT_TYPE + "=" + p);
	}

	@Override
	public String toString() {
		return mapping.toString();
	}
}
