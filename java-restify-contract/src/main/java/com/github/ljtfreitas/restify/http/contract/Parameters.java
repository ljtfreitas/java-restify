/*******************************************************************************
 *
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

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Parameters implements Iterable<Parameters.Parameter> {

	private static final String DEFAULT_PREFIX = "";

	private final String prefix;
	private final Map<String, Parameter> parameters;

	public Parameters() {
		this(DEFAULT_PREFIX);
	}

	public Parameters(String prefix) {
		this.prefix = prefix;
		this.parameters = new LinkedHashMap<>();
	}

	public Parameters(Collection<Parameter> parameters) {
		this(DEFAULT_PREFIX, parameters);
	}

	public Parameters(String prefix, Collection<Parameter> parameters) {
		this(prefix);
		parameters.forEach(p -> add(p.name, p.values));
	}

	public Parameters(Parameter... parameters) {
		this(DEFAULT_PREFIX, parameters);
	}

	public Parameters(String prefix, Parameter... parameters) {
		this(prefix);
		Arrays.stream(parameters).forEach(p -> add(p.name, p.values));
	}

	private Parameters(String prefix, Map<String, Parameter> parameters) {
		this.prefix = prefix;
		this.parameters = new LinkedHashMap<>(parameters);
	}

	public Parameters put(String name, String value) {
		Parameters parameters = new Parameters(prefix, this.parameters);
		parameters.add(name, value);
		return parameters;
	}

	public Parameters put(String name, Collection<String> values) {
		Parameters parameters = new Parameters(prefix, this.parameters);
		parameters.add(name, values);
		return parameters;
	}

	public Parameters put(Parameter parameter) {
		Parameters parameters = new Parameters(prefix, this.parameters);
		parameters.add(parameter.name, parameter.values);
		return parameters;
	}

	public Parameters putAll(Parameters source) {
		Parameters parameters = new Parameters(prefix, this.parameters);
		parameters.addAll(source);
		return parameters;
	}

	private void add(String name, String value) {
		parameters.compute(prefix + name,
			(key, parameter) ->
				Optional.ofNullable(parameter).map(p -> p.add(value))
					.orElseGet(() -> new Parameter(key, value)));
	}

	private void add(String name, Collection<String> values) {
		values.forEach(value -> add(name, value));
	}

	private void addAll(Parameters source) {
		source.forEach(p ->
			p.values.forEach(value ->
				add(p.name(), value)));
	}

	public Optional<String> first(String name) {
		return doFirst(name).flatMap(p -> Optional.ofNullable(p.value()));
	}

	public Optional<Parameter> get(String name) {
		return doFirst(name);
	}

	private Optional<Parameter> doFirst(String name) {
		return Optional.ofNullable(parameters.get(name));
	}

	public Collection<Parameter> all() {
		return parameters.values();
	}

	public String queryString() {
		return parameters.values().stream().map(Parameter::queryString).collect(Collectors.joining("&"));
	}

	@Override
	public String toString() {
		return parameters.toString();
	}

	@Override
	public Iterator<Parameter> iterator() {
		return parameters.values().iterator();
	}

	public static Parameters parse(String source) {
		Parameters parameters = new Parameters();

		String safe = Optional.ofNullable(source).orElse("");

		Arrays.stream(safe.split("&"))
			.map(p -> p.split("="))
				.filter(p -> p.length == 2)
					.forEach(p -> parameters.add(p[0], p[1]));

		return parameters;
	}

	public static Parameters of(Map<String, ?> source) {
		Parameters parameters = new Parameters();

		source.forEach((key, value) -> {
			if (value instanceof Iterable) {
				((Iterable<?>) value).forEach(e -> parameters.add(key, e.toString()));
			} else {
				parameters.add(key, value.toString());
			}
		});

		return parameters;
	}

	public static Parameters of(Parameter... source) {
		return new Parameters(source);
	}

	public static Parameters of(Collection<Parameter> source) {
		return new Parameters(source);
	}

	public static Parameters empty() {
		return new Parameters();
	}

	public static class Parameter {

		private final String name;
		private final Collection<String> values;

		public Parameter(String name, String value) {
			this(name, Arrays.asList(nonNull(value)));
		}

		public Parameter(String name, Collection<String> values) {
			this.name = name;
			this.values = new LinkedHashSet<>(values);
		}

		public String name() {
			return name;
		}

		public Collection<String> values() {
			return values;
		}

		public String value() {
			return values.stream().findFirst().orElse(null);
		}

		private Parameter add(String value) {
			Parameter parameter = new Parameter(this.name, this.values);
			parameter.values.add(value);
			return parameter;
		}

		public static Parameter of(String name, String value) {
			return new Parameter(name, value);
		}

		public static Parameter of(String name, Collection<String> values) {
			return new Parameter(name, values);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, values);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Parameter)) return false;

			Parameter that = (Parameter) obj;
			return this.name.equals(that.name)
				&& this.values.equals(that.values);
		}

		public String queryString() {
			return values.stream()
					.map(value -> name + "=" + encode(value))
						.collect(Collectors.joining("&"));
		}

		private String encode(String value) {
			try {
				return URLEncoder.encode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return value;
			}
		}
		
		@Override
		public String toString() {
			return values.stream()
					.map(value -> name + "=" + value)
						.collect(Collectors.joining("&", "Parameter:[", "]"));
		}
	}
}
