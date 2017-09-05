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
import java.net.URL;
import java.util.Collection;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.contract.Cookie;
import com.github.ljtfreitas.restify.http.contract.Cookies;

public class AuthorizationGrantProperties extends GrantProperties {

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

		private final AuthorizationGrantProperties properties;
		private final GrantProperties.Builder delegate;

		public Builder() {
			this.properties = new AuthorizationGrantProperties();
			this.delegate = new GrantProperties.Builder(properties);
		}

		public Builder(AuthorizationGrantProperties properties) {
			this.properties = properties;
			this.delegate = new GrantProperties.Builder(properties);
		}

		public Builder resource(Resource resource) {
			delegate.resource(resource);
			return this;
		}

		public Builder resource(String id) {
			delegate.resource(id);
			return this;
		}

		public Builder resource(String id, URL url) {
			delegate.resource(id, url);
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
			properties.authorizationCode = authorizationCode;
			return this;
		}

		public Builder authorizationUri(URI authorizationUri) {
			properties.authorizationUri = authorizationUri;
			return this;
		}

		public Builder authorizationUri(String authorizationUri) {
			properties.authorizationUri = URI.create(authorizationUri);
			return this;
		}

		public Builder redirectUri(URI redirectUri) {
			properties.redirectUri = redirectUri;
			return this;
		}

		public Builder redirectUri(String redirectUri) {
			properties.redirectUri = URI.create(redirectUri);
			return this;
		}

		public Builder responseType(String responseType) {
			properties.responseType = responseType;
			return this;
		}

		public Builder cookie(String cookie) {
			properties.cookie = cookie;
			return this;
		}

		public Builder cookie(Cookie cookie) {
			properties.cookie = cookie.toString();
			return this;
		}

		public Builder cookie(String name, String value) {
			properties.cookie = new Cookie(name, value).toString();
			return this;
		}

		public Builder cookies(Cookies cookies) {
			properties.cookie = cookies.toString();
			return this;
		}

		public Builder state(String state) {
			properties.state = state;
			return this;
		}

		public AuthorizationGrantProperties build() {
			return properties;
		}
	}
}
