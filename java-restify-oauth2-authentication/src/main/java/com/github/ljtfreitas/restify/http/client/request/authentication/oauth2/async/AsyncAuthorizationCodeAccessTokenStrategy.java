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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCode;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCodeGrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;

public class AsyncAuthorizationCodeAccessTokenStrategy implements AsyncAccessTokenStrategy {

	private final AsyncAuthorizationCodeProvider authorizationCodeProvider;

	public AsyncAuthorizationCodeAccessTokenStrategy(AsyncAuthorizationCodeProvider authorizationCodeProvider) {
		this.authorizationCodeProvider = authorizationCodeProvider;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletionStage<AccessTokenRequest> newAccessTokenRequest(OAuth2AuthenticatedEndpointRequest request) {
		AuthorizationCodeGrantProperties properties = request.properties(AuthorizationCodeGrantProperties.class);

		CompletionStage<AuthorizationCode> authorizationCodeAsFuture = properties.authorizationCode()
				.map(CompletableFuture::completedFuture)
					.map(CompletionStage.class::cast)
						.orElseGet(() -> authorizationCodeProvider.provides(request));

		return authorizationCodeAsFuture.thenApply(authorizationCode -> {
			AccessTokenRequest.Builder builder = AccessTokenRequest.authorizationCode(authorizationCode);

			if (properties.redirectUri().isPresent()) {
				builder.parameter("redirect_uri", properties.redirectUri().get().toString());
			}

			return builder.credentials(properties.credentials())
					.accessTokenUri(properties.accessTokenUri())
					.build();
		});
	}

}
