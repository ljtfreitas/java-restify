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
package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.async;

import java.util.concurrent.CompletionStage;

import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessToken;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCodeRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ImplicitAuthorizationResponse;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ImplicitGrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;

public class AsyncImplicitAccessTokenProvider implements AsyncAccessTokenProvider {

	private final AsyncAuthorizationServer authorizationServer;

	public AsyncImplicitAccessTokenProvider() {
		this(new DefaultAsyncAuthorizationServer());
	}

	public AsyncImplicitAccessTokenProvider(AsyncAuthorizationServer authorizationServer) {
		this.authorizationServer = authorizationServer;
	}

	@Override
	public CompletionStage<AccessToken> provides(OAuth2AuthenticatedEndpointRequest request) {
		ImplicitGrantProperties properties = request.properties(ImplicitGrantProperties.class);

		return authorizationServer.authorize(new AuthorizationCodeRequest(properties, request.scope()))
			.thenApply(authorizeResponse -> new ImplicitAuthorizationResponse(properties, authorizeResponse))
				.thenApply(ImplicitAuthorizationResponse::accessToken);
	}

	@Override
	public CompletionStage<AccessToken> refresh(AccessToken accessToken, OAuth2AuthenticatedEndpointRequest request) {
		throw new UnsupportedOperationException("Implicit Grant does not support refresh token.");
	}
}
