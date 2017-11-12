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
package com.github.ljtfreitas.restify.http.contract.metadata;

import static com.github.ljtfreitas.restify.util.Preconditions.isFalse;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EndpointMethodParameters {

	private final Map<Integer, EndpointMethodParameter> parameters = new LinkedHashMap<>();

	public Optional<EndpointMethodParameter> get(int position) {
		return Optional.ofNullable(parameters.get(position));
	}

	public Optional<EndpointMethodParameter> find(String name) {
		return parameters.values().stream().filter(p -> p.is(name)).findFirst();
	}

	public Optional<EndpointMethodParameter> ofBody() {
		return parameters.values().stream().filter(p -> p.body()).findFirst();
	}

	public Collection<EndpointMethodParameter> ofQuery() {
		return parameters.values().stream().filter(p -> p.query())
				.collect(Collectors.toList());
	}

	public Collection<EndpointMethodParameter> callbacks() {
		return parameters.values().stream().filter(p -> p.callback())
				.collect(Collectors.toList());
	}

	public Collection<EndpointMethodParameter> callbacks(Class<?> callbackClassType) {
		return parameters.values().stream()
				.filter(p -> p.callback() && callbackClassType.isAssignableFrom(p.javaType().classType()))
					.collect(Collectors.toList());
	}

	public void put(EndpointMethodParameter parameter) {
		if (parameter.body()) {
			isFalse(parameters.values().stream().anyMatch(EndpointMethodParameter::body), "Only one parameter annotated with @BodyParameter is allowed.");
		}

		parameters.put(parameter.position(), parameter);
	}
}
