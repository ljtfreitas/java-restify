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
package com.github.ljtfreitas.restify.http.client.message;

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.util.Objects;

public class ETag {

	private static final String ETAG_ANY_RESOURCE = "*";
	
	private final String value;
	private final String formatted;
	private final ETagEquality equality;

	private ETag(String value, String formatted, ETagEquality equality) {
		this.value = value;
		this.formatted = formatted;
		this.equality = equality;
	}

	public String raw() {
		return value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ETag) {
			return equality.equals(this, (ETag) obj);

		} else return false;
	}
	
	@Override
	public String toString() {
		return formatted;
	}

	public static ETag of(String tag) {
		nonNull(tag, "ETag value cannot be null.");
		return new ETag(tag, format(tag, false), (current, other) -> current.value.equals(other.value));
	}
	
	public static ETag weak(String tag) {
		nonNull(tag, "ETag value cannot be null.");
		return new ETag(tag, format(tag, true), (current, other) -> current.value.equalsIgnoreCase(other.value));
	}

	public static ETag any() {
		return new ETag(ETAG_ANY_RESOURCE, ETAG_ANY_RESOURCE, (current, other) -> true);
	}
	
	private static String format(String tag, boolean weak) {
		return new StringBuilder()
			.append(weak ? "W/" : "")
			.append("\"")
			.append(tag)
			.append("\"") 
			.toString();
	}
	
	@FunctionalInterface
	private interface ETagEquality {
		
		boolean equals(ETag current, ETag other);
	}
}
