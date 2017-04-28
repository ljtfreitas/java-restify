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

import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

public class DefaultAuthorizationCodeAccessTokenProvider implements OAuth2AccessTokenProvider {

	private final OAuth2AuthorizationConfiguration configuration;
	private final OAuth2AuthorizationCodeProvider authorizationCodeProvider;
	private final OAuth2EndpointRequestExecutor executor;

	public DefaultAuthorizationCodeAccessTokenProvider(OAuth2AuthorizationConfiguration configuration) {
		this(configuration, new DefaultOAuth2EndpointRequestExecutor());
	}

	public DefaultAuthorizationCodeAccessTokenProvider(OAuth2AuthorizationConfiguration configuration,
			OAuth2AuthorizationCodeProvider authorizationCodeProvider) {
		this(configuration, authorizationCodeProvider, new DefaultOAuth2EndpointRequestExecutor());
	}

	public DefaultAuthorizationCodeAccessTokenProvider(OAuth2AuthorizationConfiguration configuration,
			OAuth2EndpointRequestExecutor executor) {
		this(configuration, new DefaultAuthorizationCodeProvider(configuration, executor), executor);
	}

	public DefaultAuthorizationCodeAccessTokenProvider(OAuth2AuthorizationConfiguration configuration,
			OAuth2AuthorizationCodeProvider authorizationCodeProvider, OAuth2EndpointRequestExecutor executor) {
		this.configuration = configuration;
		this.authorizationCodeProvider = authorizationCodeProvider;
		this.executor = executor;
	}

	@Override
	public OAuth2AccessToken get() {
		String authorizationCode = configuration.authorizationCode()
				.orElseGet(() -> authorizationCodeProvider.get());

		return execute(buildAccessTokenRequest(authorizationCode));
	}

	private OAuth2AccessToken execute(OAuth2AccessTokenRequest request) {
		EndpointResponse<OAuth2AccessToken> response = executor.requireToken(request);
		return response.body();
	}

	private OAuth2AccessTokenRequest buildAccessTokenRequest(String authorizationCode) {
		OAuth2AccessTokenRequest.Builder builder = OAuth2AccessTokenRequest.authorizationCode(authorizationCode);

		if (configuration.redirectUri().isPresent()) {
			builder.put("redirect_uri", configuration.redirectUri().get().toString());
		}

		return builder.credentials(configuration.credentials())
					  .accessTokenUri(configuration.accessTokenUri())
					  .build();
	}
}
