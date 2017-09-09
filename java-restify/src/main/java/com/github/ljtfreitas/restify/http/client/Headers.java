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
package com.github.ljtfreitas.restify.http.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

public class Headers {

	public static final String ACCEPT = "Accept";
	public static final String AUTHORIZATION = "Authorization";
	public static final String CONNECTION = "Connection";
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String HOST = "Host";

	private final Collection<Header> headers;

	public Headers() {
		this(new LinkedHashSet<>());
	}

	public Headers(Header... headers) {
		this(new LinkedHashSet<>(Arrays.asList(headers)));
	}

	public Headers(Headers source) {
		this(new LinkedHashSet<>(source.headers));
	}

	private Headers(Collection<Header> headers) {
		this.headers = headers;
	}

	public void add(Header header) {
		headers.add(header);
	}

	public void put(String name, String value) {
		headers.add(new Header(name, value));
	}

	public void put(String name, Collection<String> values) {
		values.forEach(value -> headers.add(new Header(name, value)));
	}

	public void put(Header header) {
		headers.add(header);
	}

	public void putAll(Headers headers) {
		this.headers.addAll(headers.all());
	}

	public void replace(String name, String value) {
		headers.removeIf(h -> h.name().equalsIgnoreCase(name));
		headers.add(new Header(name, value));
	}

	public Collection<Header> all() {
		return Collections.unmodifiableCollection(headers);
	}

	public Optional<Header> get(String name) {
		return headers.stream().filter(h -> h.name().equalsIgnoreCase(name))
				.findFirst();
	}

	@Override
	public String toString() {
		return headers.toString();
	}

	public static Headers empty() {
		return new Headers(Collections.emptySet());
	}
}
