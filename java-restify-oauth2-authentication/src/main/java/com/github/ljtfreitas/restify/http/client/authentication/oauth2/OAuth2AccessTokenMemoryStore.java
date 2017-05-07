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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class OAuth2AccessTokenMemoryStore implements OAuth2AccessTokenStore {

	private final Map<OAuth2AccessTokenKey, OAuth2AccessToken> tokens = new ConcurrentHashMap<>();

	@Override
	public Optional<OAuth2AccessToken> findBy(OAuth2DelegateUser user, OAuth2Configuration configuration) {
		return Optional.ofNullable(tokens.get(key(user, configuration)));
	}

	@Override
	public void add(OAuth2DelegateUser user, OAuth2Configuration configuration, OAuth2AccessToken accessToken) {
		tokens.put(key(user, configuration), accessToken);
	}

	private OAuth2AccessTokenKey key(OAuth2DelegateUser user, OAuth2Configuration configuration) {
		String userKey = Optional.ofNullable(user).map(OAuth2DelegateUser::identity).orElse(null);
		String resourceKey = configuration.resourceKey().orElse(null);
		String clientId = configuration.credentials().clientId();
		String scope = configuration.scope();

		return new OAuth2AccessTokenKey(userKey, resourceKey, clientId, scope);
	}

	private class OAuth2AccessTokenKey {

		private final String userKey;
		private final String resourceKey;
		private final String clientId;
		private final String scope;

		private OAuth2AccessTokenKey(String userKey, String resourceKey, String clientId, String scope) {
			this.userKey = userKey;
			this.resourceKey = resourceKey;
			this.clientId = clientId;
			this.scope = scope;
		}

		@Override
		public int hashCode() {
			return Objects.hash(userKey, resourceKey, clientId, scope);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof OAuth2AccessTokenKey) {
				OAuth2AccessTokenKey that = (OAuth2AccessTokenKey) obj;

				return Objects.equals(this.userKey, that.userKey)
					&& Objects.equals(this.resourceKey, that.resourceKey)
					&& Objects.equals(this.clientId, that.clientId)
					&& Objects.equals(this.scope, that.scope);

			} else {
				return false;
			}
		}
	}
}
