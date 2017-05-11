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
package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.contract.Cookie;
import com.github.ljtfreitas.restify.http.contract.Cookies;

public class AuthorizationResource extends Resource {

	private String authorizationCode;
	private URI authorizationUri;
	private URI redirectUri;
	private String responseType;
	private String cookie;
	private String state;

	public Optional<String> authorizationCode() {
		return Optional.ofNullable(authorizationCode);
	}

	public URI authorizationUri() {
		return authorizationUri;
	}

	public Optional<URI> redirectUri() {
		return Optional.ofNullable(redirectUri);
	}

	public String responseType() {
		return responseType;
	}

	public Optional<String> cookie() {
		return Optional.ofNullable(cookie);
	}

	public Optional<String> state() {
		return Optional.ofNullable(state);
	}

	public static class Builder {

		private final AuthorizationResource configuration = new AuthorizationResource();
		private final Resource.Builder delegate = new Resource.Builder(configuration);

		public Builder id(String id) {
			delegate.id(id);
			return this;
		}

		public Builder accessTokenUri(String accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public Builder accessTokenUri(URI accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public Builder clientId(String clientId) {
			delegate.clientId(clientId);
			return this;
		}

		public Builder credentials(ClientCredentials credentials) {
			delegate.credentials(credentials);
			return this;
		}

		public Builder scopes(Collection<String> scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public Builder scopes(String... scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public Builder authorizationCode(String authorizationCode) {
			configuration.authorizationCode = authorizationCode;
			return this;
		}

		public Builder authorizationUri(URI authorizationUri) {
			configuration.authorizationUri = authorizationUri;
			return this;
		}

		public Builder authorizationUri(String authorizationUri) {
			configuration.authorizationUri = URI.create(authorizationUri);
			return this;
		}

		public Builder redirectUri(URI redirectUri) {
			configuration.redirectUri = redirectUri;
			return this;
		}

		public Builder redirectUri(String redirectUri) {
			configuration.redirectUri = URI.create(redirectUri);
			return this;
		}

		public Builder responseType(String responseType) {
			configuration.responseType = responseType;
			return this;
		}

		public Builder cookie(String cookie) {
			configuration.cookie = cookie;
			return this;
		}

		public Builder cookie(Cookie cookie) {
			configuration.cookie = cookie.toString();
			return this;
		}

		public Builder cookie(String name, String value) {
			configuration.cookie = new Cookie(name, value).toString();
			return this;
		}

		public Builder cookies(Cookies cookies) {
			configuration.cookie = cookies.toString();
			return this;
		}

		public Builder state(String state) {
			configuration.state = state;
			return this;
		}

		public AuthorizationResource build() {
			return configuration;
		}
	}
}
