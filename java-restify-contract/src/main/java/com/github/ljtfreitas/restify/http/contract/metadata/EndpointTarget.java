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

import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;
import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class EndpointTarget {

	private final Class<?> type;
	private final String endpoint;

	public EndpointTarget(Class<?> type) {
		this(type, null);
	}

	public EndpointTarget(Class<?> type, String endpoint) {
		nonNull(type, "EndpointTarget type cannot be null.");
		isTrue(type.isInterface(), "EndpointTarget type must be a interface.");

		this.type = type;
		this.endpoint = endpoint;
	}

	public Class<?> type() {
		return type;
	}

	public Optional<String> endpoint() {
		return Optional.ofNullable(endpoint);
	}

	public Collection<Method> methods() {
		return Arrays.stream(type.getMethods())
				.filter(javaMethod -> javaMethod.getDeclaringClass() != Object.class
					|| !javaMethod.isDefault()
					|| !Modifier.isStatic(javaMethod.getModifiers()))
				.collect(Collectors.toSet());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EndpointTarget) {
			EndpointTarget that = (EndpointTarget) obj;
			return type.equals(that.type) && Objects.equals(endpoint, that.endpoint);

		} else return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, endpoint);
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("EndpointTarget: [")
				.append("Java Type: ")
					.append(type)
				.append(", ")
				.append("Endpoint: ")
					.append(endpoint == null ? "(empty)" : endpoint)
			.append("]");

		return report.toString();
	}
}
