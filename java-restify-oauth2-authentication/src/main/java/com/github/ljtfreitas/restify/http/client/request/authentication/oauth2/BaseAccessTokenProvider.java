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

import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenRequest.Builder;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

abstract class BaseAccessTokenProvider implements AccessTokenProvider {

	private final AuthorizationServer authorizationServer;

	protected BaseAccessTokenProvider() {
		this(new DefaultAuthorizationServer());
	}

	protected BaseAccessTokenProvider(AuthorizationServer authorizationServer) {
		this.authorizationServer = authorizationServer;
	}

	@Override
	public AccessToken provides(OAuth2AuthenticatedEndpointRequest request) {
		EndpointResponse<AccessToken> response = authorizationServer.requireToken(buildAccessTokenRequest(request));
		return response.body();
	}

	@Override
	public AccessToken refresh(AccessToken accessToken, OAuth2AuthenticatedEndpointRequest request) {
		EndpointResponse<AccessToken> response = authorizationServer.requireToken(buildRefreshTokenRequest(accessToken, request));
		return response.body();
	}

	protected AccessTokenRequest buildRefreshTokenRequest(AccessToken accessToken, OAuth2AuthenticatedEndpointRequest request) {
		isTrue(accessToken.refreshToken().isPresent(), "Your access token must have a refresh token.");

		Builder builder = AccessTokenRequest.refreshToken(accessToken.refreshToken().get());

		return builder.accessTokenUri(request.accessTokenUri())
					  .credentials(request.credentials())
					  .build();
	}

	protected abstract AccessTokenRequest buildAccessTokenRequest(OAuth2AuthenticatedEndpointRequest request);
}
