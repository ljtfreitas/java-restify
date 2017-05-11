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

class DefaultAuthorizationCodeProvider implements AuthorizationCodeProvider {

	private final AuthorizationResource configuration;
	private final AuthorizationServer authorizationServer;

	public DefaultAuthorizationCodeProvider(AuthorizationResource configuration, AuthorizationServer authorizationServer) {
		this.configuration = configuration;
		this.authorizationServer = authorizationServer;
	}

	public DefaultAuthorizationCodeProvider(AuthorizationResource configuration) {
		this.configuration = configuration;
		this.authorizationServer = new DefaultAuthorizationServer();
	}

	@Override
	public String provides() {
		EndpointResponse<String> authorizationResponse = authorizationServer.authorize(configuration);

		StatusCode status = authorizationResponse.code();

		if (status.isOK()) {
			String message = "Do you approve the client [" + configuration.credentials().clientId() + "] to access your resources "
				+ "with scopes [" + configuration.scopes() + "].";

			throw new OAuth2UserApprovalRequiredException(message);

		} else {
			Header location = authorizationResponse.headers().get("Location")
					.orElseThrow(() -> new IllegalStateException("Location header must be present on Authorization redirect!"));

			URI redirectUri = URI.create(location.value());

			Parameters parameters = Parameters.parse(redirectUri.getQuery());

			isTrue(configuration.state().orElse("").equals(parameters.get("state").orElse("")),
					"Possible CSRF attack? [state] parameter returned by the authorization server is not the same of the authorization request.");

			String code = parameters.get("code")
					.orElseThrow(() -> new OAuth2UserRedirectRequiredException("A redirect to [" + redirectUri + "] is required!"));

			return code;
		}
	}
}
