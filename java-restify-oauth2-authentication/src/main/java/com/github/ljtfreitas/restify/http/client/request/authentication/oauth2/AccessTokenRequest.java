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

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.net.URI;
import java.net.URL;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.http.contract.Parameters.Parameter;
import com.github.ljtfreitas.restify.util.Tryable;

public class AccessTokenRequest {

	private final URI uri;
	private final ClientCredentials credentials;
	private final Parameters parameters;
	private final Headers headers;

	public AccessTokenRequest(URI uri, ClientCredentials credentials, Parameters parameters, Headers headers) {
		this.uri = uri;
		this.credentials = credentials;
		this.parameters = parameters;
		this.headers = headers;
	}

	public URI uri() {
		return uri;
	}

	public ClientCredentials credentials() {
		return credentials;
	}

	public Parameters parameters() {
		return parameters;
	}

	public Headers headers() {
		return headers;
	}

	public static AccessTokenRequest.Builder authorizationCode(String authorizationCode) {
		Builder builder = new AccessTokenRequest.Builder("authorization_code");
		builder.parameter("code", authorizationCode);

		return builder;
	}

	public static AccessTokenRequest.Builder clientCredentials(ClientCredentials credentials) {
		Builder builder = new AccessTokenRequest.Builder("client_credentials");
		builder.credentials(credentials);

		return builder;
	}

	public static AccessTokenRequest.Builder resourceOwner(ResourceOwner resourceOwner) {
		Builder builder = new AccessTokenRequest.Builder("password");

		builder.parameter("username", resourceOwner.username())
			   .parameter("password", resourceOwner.password());

		return builder;
	}

	public static AccessTokenRequest.Builder refreshToken(String refreshToken) {
		Builder builder = new AccessTokenRequest.Builder("refresh_token");

		builder.parameter("refresh_token", refreshToken);

		return builder;
	}

	public static class Builder {

		private URI accessTokenUri = null;
		private ClientCredentials credentials = null;
		private Parameters parameters = null;
		private Headers headers = new Headers();

		private Builder(String grantType) {
			parameters = Parameters.of(new Parameter("grant_type", grantType));
		}

		public Builder parameter(String name, String value) {
			parameters = parameters.put(name, value);
			return this;
		}

		public Builder parameter(Parameter parameter) {
			parameters = parameters.put(parameter.name(), parameter.value());
			return this;
		}

		public Builder credentials(ClientCredentials credentials) {
			this.credentials = credentials;
			return this;
		}

		public Builder accessTokenUri(URI acessTokenUri) {
			this.accessTokenUri = acessTokenUri;
			return this;
		}

		public Builder accessTokenUri(URL acessTokenUri) {
			this.accessTokenUri = Tryable.of(acessTokenUri::toURI);
			return this;
		}

		public Builder accessTokenUri(String acessTokenUri) {
			this.accessTokenUri = URI.create(acessTokenUri);
			return this;
		}

		public Builder header(String name, String value) {
			headers.add(name, value);
			return this;
		}

		public Builder header(Header header) {
			headers.add(header);
			return this;
		}

		public AccessTokenRequest build() {
			nonNull(credentials, "Your client credentials (client_id and client_secret) are required.");
			nonNull(accessTokenUri, "The access token URI of authorization server is required.");

			return new AccessTokenRequest(accessTokenUri, credentials, parameters, headers);
		}
	}
}
