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

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.http.contract.Parameters.Parameter;

public class AuthorizationCodeEndpointRequestFactory {

	private final AuthorizationCodeRequest request;

	public AuthorizationCodeEndpointRequestFactory(AuthorizationCodeRequest request) {
		this.request = request;
	}

	public EndpointRequest create() {
		nonNull(request.credentials().clientId(), "Your Client ID is required.");
		nonNull(request.authorizationUri(), "The authorization URI of authorization server is required.");
		nonNull(request.responseType(), "The response_type parameter is required.");

		Parameters parameters = Parameters.of(parametersOf(request));

		Headers headers = new Headers(headersOf(request));

		if (request.cookie().isPresent()) {
			headers.add("Cookie", request.cookie().get());
		}

		URI authorizationUri = URI.create(request.authorizationUri().toString() + "?" + parameters.queryString());

		return new EndpointRequest(authorizationUri, "GET", headers, String.class);
	}

	private Collection<Parameter> parametersOf(AuthorizationCodeRequest request) {
		Collection<Parameter> parameters = new ArrayList<>();

		parameters.add(Parameter.of("response_type", request.responseType()));
		parameters.add(Parameter.of("client_id", request.credentials().clientId()));
		parameters.add(Parameter.of("scope", request.scope()));

		if (request.redirectUri().isPresent()) {
			parameters.add(Parameter.of("redirect_uri", request.redirectUri().get().toString()));
		}

		if (request.state().isPresent()) {
			parameters.add(Parameter.of("state", request.state().get()));
		}

		return parameters;
	}

	private Collection<Header> headersOf(AuthorizationCodeRequest request) {
		Collection<Header> headers = new ArrayList<>(request.headers().all());

		if (request.cookie().isPresent()) {
			headers.add(Header.cookie(request.cookie().get()));
		}

		return headers;
	}
}
