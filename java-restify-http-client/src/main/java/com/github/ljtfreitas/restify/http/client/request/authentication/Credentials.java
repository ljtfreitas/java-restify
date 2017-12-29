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
package com.github.ljtfreitas.restify.http.client.request.authentication;

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Credentials {

	private final String username;
	private final String password;

	public Credentials(String username, String password) {
		this.username = nonNull(username, "Username is required.");
		this.password = nonNull(password, "Password is required");
	}

	public String username() {
		return username;
	}

	public String password() {
		return password;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Credentials) {
			Credentials that = (Credentials) obj;
			return this.username.equals(that.username);

		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(username);
	}

	@Override
	public String toString() {
		return username + " : " + Stream.generate(() -> "*").limit(password.length()).collect(Collectors.joining(""));
	}
}
