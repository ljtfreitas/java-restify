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

public class AuthorizationCodeAccessTokenProvider extends BaseAccessTokenProvider {

	private final AuthorizationGrantProperties configuration;
	private final AuthorizationCodeProvider authorizationCodeProvider;

	public AuthorizationCodeAccessTokenProvider(AuthorizationGrantProperties configuration) {
		this(configuration, new DefaultAuthorizationServer());
	}

	public AuthorizationCodeAccessTokenProvider(AuthorizationGrantProperties configuration,
			AuthorizationCodeProvider authorizationCodeProvider) {
		this(configuration, authorizationCodeProvider, new DefaultAuthorizationServer());
	}

	public AuthorizationCodeAccessTokenProvider(AuthorizationGrantProperties configuration,
			AuthorizationServer authorizationServer) {
		this(configuration, new DefaultAuthorizationCodeProvider(configuration, authorizationServer), authorizationServer);
	}

	public AuthorizationCodeAccessTokenProvider(AuthorizationGrantProperties configuration,
			AuthorizationCodeProvider authorizationCodeProvider, AuthorizationServer authorizationServer) {
		super(configuration, authorizationServer);
		this.configuration = configuration;
		this.authorizationCodeProvider = authorizationCodeProvider;
	}

	@Override
	protected AccessTokenRequest buildAccessTokenRequest() {
		String authorizationCode = configuration.authorizationCode()
					.orElseGet(() -> authorizationCodeProvider.provides());

		AccessTokenRequest.Builder builder = AccessTokenRequest.authorizationCode(authorizationCode);

		if (configuration.redirectUri().isPresent()) {
			builder.parameter("redirect_uri", configuration.redirectUri().get().toString());
		}

		return builder.credentials(configuration.credentials())
					  .accessTokenUri(configuration.accessTokenUri())
					  .build();
	}
}
