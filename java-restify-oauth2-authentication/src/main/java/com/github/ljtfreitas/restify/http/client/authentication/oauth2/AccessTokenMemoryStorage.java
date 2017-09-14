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

import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class AccessTokenMemoryStorage implements AccessTokenStorage {

	private final Map<AccessTokenKey, AccessToken> tokens = new ConcurrentHashMap<>();

	@Override
	public Optional<AccessToken> findBy(OAuth2AuthenticatedEndpointRequest request) {
		return Optional.ofNullable(tokens.get(key(request)));
	}

	@Override
	public void add(OAuth2AuthenticatedEndpointRequest request, AccessToken accessToken) {
		tokens.put(key(request), accessToken);
	}

	private AccessTokenKey key(OAuth2AuthenticatedEndpointRequest request) {
		String resource = request.endpoint().getHost();
		String clientId = request.clientId();
		String scope = request.scope();
		String username = request.user().map(Principal::getName).orElse(null);

		return new AccessTokenKey(resource, clientId, scope, username);
	}

	private class AccessTokenKey {

		private final String resourceServer;
		private final String clientId;
		private final String scope;
		private final String username;

		private AccessTokenKey(String resourceServer, String clientId, String scope, String username) {
			this.resourceServer = resourceServer;
			this.clientId = clientId;
			this.scope = scope;
			this.username = username;
		}

		@Override
		public int hashCode() {
			return Objects.hash(resourceServer, clientId, scope, username);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof AccessTokenKey) {
				AccessTokenKey that = (AccessTokenKey) obj;

				return Objects.equals(this.resourceServer, that.resourceServer)
					&& Objects.equals(this.clientId, that.clientId)
					&& Objects.equals(this.scope, that.scope)
					&& Objects.equals(this.username, that.username);

			} else {
				return false;
			}
		}
	}
}
