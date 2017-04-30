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

import static com.github.ljtfreitas.restify.http.util.Preconditions.isTrue;

import java.net.URI;

import com.github.ljtfreitas.restify.http.client.Header;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;
import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.http.util.Tryable;

public class ImplicitAccessTokenProvider implements OAuth2AccessTokenProvider {

	private final OAuth2AuthorizationConfiguration configuration;
	private final OAuth2EndpointRequestExecutor executor;

	public ImplicitAccessTokenProvider(OAuth2AuthorizationConfiguration configuration) {
		this(configuration, new DefaultOAuth2EndpointRequestExecutor());
	}

	public ImplicitAccessTokenProvider(OAuth2AuthorizationConfiguration configuration, OAuth2EndpointRequestExecutor executor) {
		this.configuration = configuration;
		this.executor = executor;
	}

	@Override
	public OAuth2AccessToken get() {
		EndpointResponse<String> authorizationResponse = executor.authorize(configuration);

		StatusCode status = authorizationResponse.code();

		if (status.isOK()) {
			String message = "Do you approve the client [" + configuration.credentials().clientId() + "] to access your resources "
				+ "with scopes [" + configuration.scopes() + "]";

			throw new OAuth2UserApprovalRequiredException(message);

		} else {
			Header location = authorizationResponse.headers().get("Location")
					.orElseThrow(() -> new IllegalStateException("Location header must be present on Authorization redirect!"));

			Parameters parameters = Parameters.parse(URI.create(location.value()).getFragment());

			isTrue(configuration.state().orElse("").equals(parameters.get("state").orElse("")),
					"Possible CSRF attack? [state] parameter returned by the authorization server is not the same of the authorization request.");

			return buildAccessToken(parameters);
		}
	}

	private OAuth2AccessToken buildAccessToken(Parameters parameters) {
		String token = parameters.get("access_token")
				.orElseThrow(() -> new IllegalStateException("Access token parameter must be present on Authorization redirect!"));

		OAuth2AccessTokenType tokenType = Tryable.or(() -> OAuth2AccessTokenType.of(parameters.get("token_type").orElse(null)),
				OAuth2AccessTokenType.BEARER);

		return new OAuth2AccessToken(tokenType, token);
	}
}
