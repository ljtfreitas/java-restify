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
import java.net.URL;
import java.security.Principal;
import java.util.Collection;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.message.Cookie;
import com.github.ljtfreitas.restify.http.client.message.Cookies;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;

public class AuthorizationCodeGrantProperties extends GrantProperties {

	private String authorizationCode;
	private URI authorizationUri;
	private URI redirectUri;
	private String responseType = "code";
	private String cookie;
	private String state;
	private Headers headers = new Headers();

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

	public Headers headers() {
		return headers;
	}

	public static class Builder {

		private final AuthorizationCodeGrantProperties properties;
		private final GrantProperties.Builder delegate;

		public Builder() {
			this.properties = new AuthorizationCodeGrantProperties();
			this.delegate = new GrantProperties.Builder(properties);
		}

		public Builder(AuthorizationCodeGrantProperties properties) {
			this.properties = properties;
			this.delegate = new GrantProperties.Builder(properties);
		}

		public Builder accessTokenUri(String accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public Builder accessTokenUri(URI accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public Builder accessTokenUri(URL acessTokenUri) {
			delegate.accessTokenUri(acessTokenUri);
			return this;
		}

		public Builder clientId(String clientId) {
			delegate.clientId(clientId);
			return this;
		}

		public Builder credentials(String clientId, String clientSecret) {
			delegate.credentials(new ClientCredentials(clientId, clientSecret));
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

		public Builder user(Principal user) {
			delegate.user(user);
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

		public Builder header(String name, String value) {
			properties.headers = properties.headers.add(name, value);
			return this;
		}

		public Builder header(Header header) {
			properties.headers = properties.headers.add(header);
			return this;
		}

		public Builder headers(Header... headers) {
			properties.headers = properties.headers.addAll(new Headers(headers));
			return this;
		}

		public Builder headers(Headers headers) {
			properties.headers = properties.headers.addAll(headers);
			return this;
		}

		public AuthorizationCodeGrantProperties build() {
			return properties;
		}
	}
}
