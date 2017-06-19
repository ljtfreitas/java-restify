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
	public static final String ACCEPT_CHARSET = "Accept-Charset";
	public static final String ACCEPT_ENCODING = "Accept-Encoding";
	public static final String ACCEPT_LANGUAGE = "Accept-Language";
	public static final String ACCEPT_RANGES = "Accept-Ranges";
	public static final String ACCEPT_VERSION = "Accept-Version";
	public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
	public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
	public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
	public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
	public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
	public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
	public static final String AGE = "Age";
	public static final String ALLOW = "Allow";
	public static final String AUTHORIZATION = "Authorization";
	public static final String CACHE_CONTROL = "Cache-Control";
	public static final String CONNECTION = "Connection";
	public static final String CONTENT_DISPOSITION = "Content-Disposition";
	public static final String CONTENT_ENCODING = "Content-Encoding";
	public static final String CONTENT_LANGUAGE = "Content-Language";
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String CONTENT_LOCATION = "Content-Location";
	public static final String CONTENT_RANGE = "Content-Range";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String COOKIE = "Cookie";
	public static final String DATE = "Date";
	public static final String DO_NOT_TRACK = "DNT";
	public static final String ETAG = "ETag";
	public static final String EXPECT = "Expect";
	public static final String EXPIRES = "Expires";
	public static final String FORWARDED = "Forwarded";
	public static final String FROM = "From";
	public static final String HOST = "Host";
	public static final String IF_MATCH = "If-Match";

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
