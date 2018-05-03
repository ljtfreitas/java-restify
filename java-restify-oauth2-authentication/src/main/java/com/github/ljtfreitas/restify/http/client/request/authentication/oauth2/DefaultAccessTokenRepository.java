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

import java.util.Optional;
import java.util.function.Supplier;

class DefaultAccessTokenRepository implements AccessTokenRepository {

	private final AccessTokenProvider accessTokenProvider;
	private final AccessTokenStorage accessTokenStorage;

	public DefaultAccessTokenRepository(AccessTokenProvider accessTokenProvider, AccessTokenStorage accessTokenStorage) {
		this.accessTokenProvider = accessTokenProvider;
		this.accessTokenStorage = accessTokenStorage;
	}

	@Override
	public AccessToken findBy(OAuth2AuthenticatedEndpointRequest request) {
		AccessTokenStorageKey key = AccessTokenStorageKey.by(request);

		Optional<AccessToken> accessToken = accessTokenStorage.findBy(key);

		if (accessToken.isPresent()) {
			if (accessToken.get().expired()) {
				if (accessToken.get().refreshToken().isPresent()) {
					return refresh(accessToken.get(), request, key);

				} else {
					return newToken(request, key);
				}

			} else {
				return accessToken.get();
			}

		} else {
			return newToken(request, key);
		}
	}

	private AccessToken refresh(AccessToken accessToken, OAuth2AuthenticatedEndpointRequest request, AccessTokenStorageKey key) {
		return store(key, () -> accessTokenProvider.refresh(accessToken, request));
	}

	private AccessToken newToken(OAuth2AuthenticatedEndpointRequest request, AccessTokenStorageKey key) {
		return store(key, () -> accessTokenProvider.provides(request));
	}

	private AccessToken store(AccessTokenStorageKey key, Supplier<AccessToken> supplier) {
		AccessToken newAccessToken = supplier.get();

		accessTokenStorage.add(key, newAccessToken);

		return newAccessToken;
	}
}
