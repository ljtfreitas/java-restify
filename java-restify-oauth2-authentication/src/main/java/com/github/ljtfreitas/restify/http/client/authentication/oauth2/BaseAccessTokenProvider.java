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

import com.github.ljtfreitas.restify.http.client.authentication.oauth2.AccessTokenRequest.Builder;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

abstract class BaseAccessTokenProvider implements AccessTokenProvider {

	private final OAuth2Configuration configuration;
	private final AuthorizationServer authorizationServer;

	protected BaseAccessTokenProvider(OAuth2Configuration configuration) {
		this(configuration, new DefaultAuthorizationServer());
	}

	protected BaseAccessTokenProvider(OAuth2Configuration configuration, AuthorizationServer authorizationServer) {
		this.configuration = configuration;
		this.authorizationServer = authorizationServer;
	}

	@Override
	public AccessToken provides() {
		EndpointResponse<AccessToken> response = authorizationServer.requireToken(buildAccessTokenRequest());
		return response.body();
	}

	@Override
	public AccessToken refresh(AccessToken accessToken) {
		EndpointResponse<AccessToken> response = authorizationServer.requireToken(buildRefreshTokenRequest(accessToken));
		return response.body();
	}

	protected AccessTokenRequest buildRefreshTokenRequest(AccessToken accessToken) {
		nonNull(accessToken.refreshToken(), "Your access token must have a refresh token.");

		Builder builder = AccessTokenRequest.refreshToken(accessToken.refreshToken());

		return builder.accessTokenUri(configuration.accessTokenUri())
					  .credentials(configuration.credentials())
					  .build();
	}

	protected abstract AccessTokenRequest buildAccessTokenRequest();
}
