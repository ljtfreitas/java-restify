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
import java.util.stream.Collectors;

public class Cookies {

	private final Collection<Cookie> cookies;

	public Cookies() {
		this.cookies = new ArrayList<>();
	}
	
	public Cookies(Cookie... cookies) {
		this.cookies = new ArrayList<>(Arrays.asList(cookies));
	}
	
	public Cookies(String... cookies) {
		this.cookies = new ArrayList<>(Arrays.stream(cookies)
				.map(c -> c.split("="))
					.filter(c -> c.length == 2)
						.map(c -> new Cookie(c[0], c[1]))
							.collect(Collectors.toList()));
	}
	
	public Cookies add(String name, String value) {
		return add(new Cookie(name, value));
	}

	public Cookies add(Cookie cookie) {
		cookies.add(cookie);
		return this;
	}
	
	@Override
	public String toString() {
		String content = cookies.stream()
			.map(Cookie::toString)
				.collect(Collectors.joining("; "));

		return content;
	}
}
