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

import static com.github.ljtfreitas.restify.http.util.Preconditions.nonNull;

import java.net.URI;

import com.github.ljtfreitas.restify.http.client.Header;
import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.http.contract.Parameters.Parameter;

public class OAuth2AccessTokenRequest {

	private final URI uri;
	private final OAuth2ClientCredentials credentials;
	private final Parameters parameters;
	private final Headers headers;

	public OAuth2AccessTokenRequest(URI uri, OAuth2ClientCredentials credentials, Parameters parameters, Headers headers) {
		this.uri = uri;
		this.credentials = credentials;
		this.parameters = parameters;
		this.headers = headers;
	}

	public URI uri() {
		return uri;
	}

	public OAuth2ClientCredentials credentials() {
		return credentials;
	}

	public Parameters parameters() {
		return parameters;
	}

	public Headers headers() {
		return headers;
	}

	public static OAuth2AccessTokenRequest.Builder authorizationCode(String authorizationCode) {
		Builder builder = new OAuth2AccessTokenRequest.Builder("authorization_code");
		builder.parameter("code", authorizationCode);

		return builder;
	}

	public static OAuth2AccessTokenRequest.Builder clientCredentials(OAuth2ClientCredentials credentials) {
		Builder builder = new OAuth2AccessTokenRequest.Builder("client_credentials");
		builder.credentials(credentials);

		return builder;
	}

	public static OAuth2AccessTokenRequest.Builder resourceOwner(OAuth2ResourceOwner resourceOwner) {
		Builder builder = new OAuth2AccessTokenRequest.Builder("password");

		builder.parameter("username", resourceOwner.username())
			   .parameter("password", resourceOwner.password());

		return builder;
	}

	public static class Builder {

		private URI accessTokenUri = null;
		private OAuth2ClientCredentials credentials = null;
		private Parameters parameters = new Parameters();
		private Headers headers = new Headers();

		private Builder(String grantType) {
			parameters.put("grant_type", grantType);
		}

		public Builder parameter(String name, String value) {
			parameters.put(name, value);
			return this;
		}

		public Builder parameter(Parameter parameter) {
			parameters.put(parameter.name(), parameter.value());
			return this;
		}

		public Builder credentials(OAuth2ClientCredentials credentials) {
			this.credentials = credentials;
			return this;
		}

		public Builder accessTokenUri(URI acessTokenUri) {
			this.accessTokenUri = acessTokenUri;
			return this;
		}

		public Builder accessTokenUri(String acessTokenUri) {
			this.accessTokenUri = URI.create(acessTokenUri);
			return this;
		}

		public Builder header(String name, String value) {
			headers.put(name, value);
			return this;
		}

		public Builder header(Header header) {
			headers.put(header);
			return this;
		}

		public OAuth2AccessTokenRequest build() {
			nonNull(credentials, "Your client credentials (client_id and client_secret) are required.");
			nonNull(accessTokenUri, "The access token URI of authorization server is required.");

			return new OAuth2AccessTokenRequest(accessTokenUri, credentials, parameters, headers);
		}
	}
}
