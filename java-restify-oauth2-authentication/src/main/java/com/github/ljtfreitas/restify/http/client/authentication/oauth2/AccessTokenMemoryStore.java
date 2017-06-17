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

class AccessTokenMemoryStore implements AccessTokenStore {

	private final Map<AccessTokenKey, AccessToken> tokens = new ConcurrentHashMap<>();

	@Override
	public Optional<AccessToken> findBy(Principal user, GrantProperties properties) {
		return Optional.ofNullable(tokens.get(key(user, properties)));
	}

	@Override
	public void add(Principal user, GrantProperties properties, AccessToken accessToken) {
		tokens.put(key(user, properties), accessToken);
	}

	private AccessTokenKey key(Principal user, GrantProperties properties) {
		String username = Optional.ofNullable(user).map(Principal::getName).orElse(null);
		String clientId = properties.credentials().clientId();
		String scope = properties.scope();

		return new AccessTokenKey(username, clientId, scope);
	}

	private class AccessTokenKey {

		private final String username;
		private final String clientId;
		private final String scope;

		private AccessTokenKey(String username, String clientId, String scope) {
			this.username = username;
			this.clientId = clientId;
			this.scope = scope;
		}

		@Override
		public int hashCode() {
			return Objects.hash(username, clientId, scope);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof AccessTokenKey) {
				AccessTokenKey that = (AccessTokenKey) obj;

				return Objects.equals(this.username, that.username)
					&& Objects.equals(this.clientId, that.clientId)
					&& Objects.equals(this.scope, that.scope);

			} else {
				return false;
			}
		}
	}
}
