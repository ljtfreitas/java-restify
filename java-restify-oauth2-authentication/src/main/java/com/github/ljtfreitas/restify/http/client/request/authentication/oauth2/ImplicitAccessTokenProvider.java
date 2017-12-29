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

import java.net.URI;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.contract.Parameters;

public class ImplicitAccessTokenProvider implements AccessTokenProvider {

	private final AuthorizationServer authorizationServer;

	public ImplicitAccessTokenProvider() {
		this(new DefaultAuthorizationServer());
	}

	public ImplicitAccessTokenProvider(AuthorizationServer authorizationServer) {
		this.authorizationServer = authorizationServer;
	}

	@Override
	public AccessToken provides(OAuth2AuthenticatedEndpointRequest request) {
		ImplicitGrantProperties properties = request.properties(ImplicitGrantProperties.class);

		EndpointResponse<String> authorizationResponse = authorizationServer.authorize(new AuthorizationCodeRequest(properties, request.scope()));

		StatusCode status = authorizationResponse.status();

		if (status.isOk()) {
			String message = "You need approve the client [" + properties.credentials().clientId() + "] to access protected resources "
				+ "with scopes [" + properties.scopes() + "]";

			throw new OAuth2UserApprovalRequiredException(message);

		} else {
			Header location = authorizationResponse.headers().get("Location")
					.orElseThrow(() -> new IllegalStateException("Location header must be present on Authorization redirect!"));

			Parameters parameters = Parameters.parse(URI.create(location.value()).getFragment());

			isTrue(properties.state().orElse("").equals(parameters.first("state").orElse("")),
					"Possible CSRF attack? [state] parameter returned by the authorization server is not the same of the authorization request.");

			return  AccessToken.of(AccessTokenResponse.of(parameters));
		}
	}

	@Override
	public AccessToken refresh(AccessToken accessToken, OAuth2AuthenticatedEndpointRequest request) {
		throw new UnsupportedOperationException("Implicit Grant does not support refresh token.");
	}
}
