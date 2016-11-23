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
package com.github.ljtfreitas.restify.http.contract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.charset.Encoding;

public class Parameters {

	private Map<String, List<String>> parameters = new LinkedHashMap<>();

	public void put(String name, String value) {
		parameters.compute(name, (k, v) -> Optional.ofNullable(v).orElseGet(() -> new ArrayList<>()))
			.add(value);
	}

	public Optional<String> first(String name) {
		return Optional.ofNullable(parameters.get(name))
			.flatMap(values -> values.stream().findFirst());
	}

	public Collection<Parameter> all() {
		return parameters.entrySet().stream()
					.map(e -> new Parameter(e.getKey(), e.getValue()))
						.collect(Collectors.toList());
	}

	public String queryString() {
		StringJoiner joiner = new StringJoiner("&");

		parameters.forEach((name, values) -> {
			values.forEach(v -> joiner.add(encode(name) + "=" + encode(v)));
		});

		return joiner.toString();
	}

	private String encode(String value) {
		return Encoding.UTF_8.encode(value);
	}

	public class Parameter {

		private final String name;
		private final Collection<String> values;

		public Parameter(String name, Collection<String> values) {
			this.name = name;
			this.values = values;
		}

		public String name() {
			return name;
		}

		public Collection<String> values() {
			return values;
		}

	}
}
