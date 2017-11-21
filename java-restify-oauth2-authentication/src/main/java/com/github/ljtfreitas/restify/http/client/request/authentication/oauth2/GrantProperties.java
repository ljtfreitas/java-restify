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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.util.Tryable;

public class GrantProperties {

	private URI accessTokenUri;
	private ClientCredentials credentials;
	private Collection<String> scopes = Collections.emptyList();
	private Principal user;

	public ClientCredentials credentials() {
		return credentials;
	}

	public Collection<String> scopes() {
		return scopes;
	}

	public String scope() {
		return scopes.stream().collect(Collectors.joining(" "));
	}

	public URI accessTokenUri() {
		return accessTokenUri;
	}

	public Optional<Principal> user() {
		return Optional.ofNullable(user);
	}

	public static class Builder {

		private final GrantProperties properties;

		public Builder() {
			this.properties = new GrantProperties();
		}

		public Builder(GrantProperties properties) {
			this.properties = properties;
		}

		public Builder accessTokenUri(String accessTokenUri) {
			properties.accessTokenUri = URI.create(accessTokenUri);
			return this;
		}

		public Builder accessTokenUri(URI accessTokenUri) {
			properties.accessTokenUri = accessTokenUri;
			return this;
		}

		public Builder accessTokenUri(URL acessTokenUri) {
			properties.accessTokenUri = Tryable.of(acessTokenUri::toURI);
			return this;
		}

		public Builder clientId(String clientId) {
			properties.credentials = ClientCredentials.clientId(clientId);
			return this;
		}

		public Builder credentials(ClientCredentials credentials) {
			properties.credentials = credentials;
			return this;
		}

		public Builder scopes(Collection<String> scopes) {
			properties.scopes = scopes;
			return this;
		}

		public Builder scopes(String... scopes) {
			properties.scopes = Arrays.asList(scopes);
			return this;
		}

		public Builder user(Principal user) {
			properties.user = user;
			return this;
		}

		public GrantProperties build() {
			return properties;
		}

		public static AuthorizationCodeGrantProperties.Builder authorizationCode() {
			return new AuthorizationCodeGrantProperties.Builder();
		}

		public static ImplicitGrantProperties.Builder implicit() {
			return new ImplicitGrantProperties.Builder();
		}

		public static ResourceOwnerGrantProperties.Builder resourceOwner() {
			return new ResourceOwnerGrantProperties.Builder();
		}

		public static ClientCredentialsGrantProperties.Builder clientCredentials() {
			return new ClientCredentialsGrantProperties.Builder();
		}
	}
}
