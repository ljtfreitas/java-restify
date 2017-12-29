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

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.util.Objects;

public class EndpointHeader {

	private final String name;
	private final String value;

	public EndpointHeader(String name, String value) {
		this.name = nonNull(name, "EndpointHeader need a name.");
		this.value = nonNull(value, "EndpointHeader need a value.");
	}

	public String name() {
		return name;
	}

	public String value() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EndpointHeader) {
			EndpointHeader that = (EndpointHeader) obj;

			return this.name.equals(that.name)
				&& this.value.equals(that.value);

		} else return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("EndpointHeader: [")
				.append("Name: ")
					.append(name)
				.append(", ")
				.append("Value: ")
					.append(value)
			.append("]");

		return report.toString();
	}
}
