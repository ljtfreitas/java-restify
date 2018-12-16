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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class Cookies {

	private final Collection<Cookie> cookies;

	public Cookies() {
		this.cookies = new ArrayList<>();
	}

	public Cookies(Cookie... cookies) {
		this(Arrays.asList(cookies));
	}

	private Cookies(Collection<Cookie> cookies) {
		this.cookies = new ArrayList<>(cookies);
	}

	public Cookies add(String name, String value) {
		Cookies cookies = new Cookies(this.cookies);
		cookies.addNew(new Cookie(name, value));
		return cookies;
	}

	public Cookies add(Cookie cookie) {
		Cookies cookies = new Cookies(this.cookies);
		cookies.addNew(cookie);
		return cookies;
	}

	private void addNew(Cookie cookie) {
		cookies.add(cookie);
	}

	public Optional<Cookie> get(String name) {
		return cookies.stream().filter(c -> c.name().equals(name)).findFirst();
	}

	public boolean empty() {
		return cookies.isEmpty();
	}

	@Override
	public String toString() {
		String content = cookies.stream()
			.map(Cookie::toString)
				.collect(Collectors.joining("; "));

		return content;
	}

	public static final Cookies of(String cookies) {
		Collection<Cookie> cookiesAsCollection = Arrays.stream(cookies.split(";"))
			.map(c -> c.split("="))
				.filter(c -> c.length == 2)
					.map(c -> new Cookie(c[0].trim(), c[1].trim()))
						.collect(Collectors.toList());

		return new Cookies(cookiesAsCollection);
	}
}
