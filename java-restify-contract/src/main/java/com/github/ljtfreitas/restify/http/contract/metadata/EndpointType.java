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

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

public class EndpointType {

	private final EndpointTarget target;
	private final EndpointMethods endpointMethods;

	public EndpointType(EndpointTarget target, EndpointMethods endpointMethods) {
		this.target = target;
		this.endpointMethods = endpointMethods;
	}

	public Class<?> javaType() {
		return target.type();
	}

	public Optional<EndpointMethod> find(Method method) {
		return endpointMethods.find(method);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EndpointType) {
			EndpointType that = (EndpointType) obj;
			return target.equals(that.target);

		} else return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(target);
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("EndpointType: [")
				.append("Target: ")
					.append(target)
				.append(", ")
				.append("Methods: ")
					.append(endpointMethods)
			.append("]");

		return report.toString();
	}
}
