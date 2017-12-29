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
import java.security.Principal;
import java.util.Collection;

public class ResourceOwnerGrantProperties extends GrantProperties {

	private ResourceOwner resourceOwner;

	public ResourceOwner resourceOwner() {
		return resourceOwner;
	}

	public static class Builder {

		private final ResourceOwnerGrantProperties properties = new ResourceOwnerGrantProperties();
		private final GrantProperties.Builder delegate = new GrantProperties.Builder(properties);

		public Builder accessTokenUri(String accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public Builder accessTokenUri(URI accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public Builder accessTokenUri(URL accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
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

		public Builder resourceOwner(ResourceOwner resourceOwner) {
			properties.resourceOwner = resourceOwner;
			return this;
		}

		public Builder resourceOwner(String username, String password) {
			properties.resourceOwner = new ResourceOwner(username, password);
			return this;
		}

		public ResourceOwnerGrantProperties build() {
			nonNull(properties.resourceOwner, "Your resource owner credentials are required.");
			return properties;
		}
	}
}
