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

public class AuthorizationCodeAccessTokenProvider extends BaseOAuth2AccessTokenProvider {

	private final OAuth2AuthorizationConfiguration configuration;
	private final OAuth2AuthorizationCodeProvider authorizationCodeProvider;

	public AuthorizationCodeAccessTokenProvider(OAuth2AuthorizationConfiguration configuration) {
		this(configuration, new DefaultOAuth2AuthorizationServer());
	}

	public AuthorizationCodeAccessTokenProvider(OAuth2AuthorizationConfiguration configuration,
			OAuth2AuthorizationCodeProvider authorizationCodeProvider) {
		this(configuration, authorizationCodeProvider, new DefaultOAuth2AuthorizationServer());
	}

	public AuthorizationCodeAccessTokenProvider(OAuth2AuthorizationConfiguration configuration,
			OAuth2AuthorizationServer authorizationServer) {
		this(configuration, new DefaultOAuth2AuthorizationCodeProvider(configuration, authorizationServer), authorizationServer);
	}

	public AuthorizationCodeAccessTokenProvider(OAuth2AuthorizationConfiguration configuration,
			OAuth2AuthorizationCodeProvider authorizationCodeProvider, OAuth2AuthorizationServer authorizationServer) {
		super(authorizationServer);
		this.configuration = configuration;
		this.authorizationCodeProvider = authorizationCodeProvider;
	}

	@Override
	protected OAuth2AccessTokenRequest buildAccessTokenRequest() {
		String authorizationCode = configuration.authorizationCode()
					.orElseGet(() -> authorizationCodeProvider.provides());

		OAuth2AccessTokenRequest.Builder builder = OAuth2AccessTokenRequest.authorizationCode(authorizationCode);

		if (configuration.redirectUri().isPresent()) {
			builder.parameter("redirect_uri", configuration.redirectUri().get().toString());
		}

		return builder.credentials(configuration.credentials())
					  .accessTokenUri(configuration.accessTokenUri())
					  .build();
	}
}
