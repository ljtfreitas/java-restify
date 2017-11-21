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
package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import java.net.URI;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.message.Headers;

public class AuthorizationCodeRequest {

	private final AuthorizationCodeGrantProperties properties;
	private final String scope;

	public AuthorizationCodeRequest(AuthorizationCodeGrantProperties properties) {
		this(properties, properties.scope());
	}

	public AuthorizationCodeRequest(AuthorizationCodeGrantProperties properties, String scope) {
		this.properties = properties;
		this.scope = scope;
	}

	public ClientCredentials credentials() {
		return properties.credentials();
	}

	public URI authorizationUri() {
		return properties.authorizationUri();
	}

	public String responseType() {
		return properties.responseType();
	}

	public String scope() {
		return scope;
	}

	public Optional<URI> redirectUri() {
		return properties.redirectUri();
	}

	public Optional<String> state() {
		return properties.state();
	}

	public Headers headers() {
		return properties.headers();
	}

	public Optional<String> cookie() {
		return properties.cookie();
	}
}
