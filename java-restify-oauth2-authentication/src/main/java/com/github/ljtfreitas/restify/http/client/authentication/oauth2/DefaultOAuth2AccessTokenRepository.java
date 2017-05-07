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

import java.util.Optional;

class DefaultOAuth2AccessTokenRepository implements OAuth2AccessTokenRepository {

	private final OAuth2AccessTokenStore accessTokenStore;
	private final OAuth2AccessTokenProvider accessTokenProvider;

	public DefaultOAuth2AccessTokenRepository(OAuth2AccessTokenStore accessTokenStore, OAuth2AccessTokenProvider accessTokenProvider) {
		this.accessTokenStore = accessTokenStore;
		this.accessTokenProvider = accessTokenProvider;
	}

	@Override
	public OAuth2AccessToken findBy(OAuth2DelegateUser user, OAuth2Configuration configuration) {
		Optional<OAuth2AccessToken> accessToken = accessTokenStore.findBy(user, configuration);

		return accessToken.filter(a -> !a.expired())
				.orElseGet(() -> newToken(user, configuration));
	}

	private OAuth2AccessToken newToken(OAuth2DelegateUser user, OAuth2Configuration configuration) {
		OAuth2AccessToken newAccessToken = accessTokenProvider.provides();

		accessTokenStore.add(user, configuration, newAccessToken);

		return newAccessToken;
	}
}
