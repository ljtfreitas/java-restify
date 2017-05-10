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

public class ClientCredentialsAccessTokenProvider extends BaseOAuth2AccessTokenProvider {

	private final OAuth2Configuration configuration;

	public ClientCredentialsAccessTokenProvider(OAuth2Configuration configuration) {
		super(configuration);
		this.configuration = configuration;
	}

	public ClientCredentialsAccessTokenProvider(OAuth2Configuration configuration, OAuth2AuthorizationServer authorizationServer) {
		super(configuration, authorizationServer);
		this.configuration = configuration;
	}

	@Override
	protected OAuth2AccessTokenRequest buildAccessTokenRequest() {
		OAuth2AccessTokenRequest.Builder builder = OAuth2AccessTokenRequest.clientCredentials(configuration.credentials());

		return builder.accessTokenUri(configuration.accessTokenUri())
					  .parameter("scope", configuration.scope())
					  .build();
	}

	@Override
	public OAuth2AccessToken refresh(OAuth2AccessToken accessToken) {
		throw new UnsupportedOperationException("Client Credentials Grant does not support refresh token.");
	}
}