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

import java.util.Base64;
import java.util.Objects;

public class BasicCredentials {

	private final Credentials credentials;

	public BasicCredentials(String username, String password) {
		this(new Credentials(username, password));
	}

	public BasicCredentials(Credentials credentials) {
		this.credentials = nonNull(credentials, "Credentials cannot be null.");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BasicCredentials) {
			BasicCredentials that = (BasicCredentials) obj;
			return this.credentials.equals(that.credentials);

		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(credentials);
	}

	@Override
	public String toString() {
		String content = credentials.username() + ":" + credentials.password();
		return "Basic " + Base64.getEncoder().encodeToString(content.getBytes());
	}
}
