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

import static com.github.ljtfreitas.restify.http.client.message.Headers.AUTHORIZATION;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.BasicAuthentication;
import com.github.ljtfreitas.restify.http.contract.Parameters;

public class AccessTokenEndpointRequestFactory {

	private final AccessTokenRequest request;
	private final ClientAuthenticationMethod clientAuthenticationMethod;

	public AccessTokenEndpointRequestFactory(AccessTokenRequest source, ClientAuthenticationMethod clientAuthenticationMethod) {
		this.request = source;
		this.clientAuthenticationMethod = clientAuthenticationMethod;
	}

	public EndpointRequest create() {
		Headers headers = new Headers(Header.contentType(AuthorizationServer.FORM_URLENCODED_CONTENT_TYPE))
				.addAll(request.headers());

		Parameters body = request.parameters();

		return authenticated(new EndpointRequest(request.uri(), "POST", headers, body, AccessTokenResponseBody.class));
	}

	private EndpointRequest authenticated(EndpointRequest endpointRequest) {
		switch (clientAuthenticationMethod) {
			case HEADER:
				return endpointRequest.add(authorization());

			case FORM_PARAMETER:
				Parameters body = ((Parameters) endpointRequest.body().get());
				return endpointRequest.usingBody(body.putAll(parameters()));

			case QUERY_PARAMETER:
				return endpointRequest.append(parameters());
		}

		return endpointRequest;
	}

	private Parameters parameters() {
		Parameters parameters = new Parameters();

		ClientCredentials credentials = request.credentials();
		parameters = parameters.put("client_id", credentials.clientId());

		if (credentials.clientSecret() != null && !"".equals(credentials.clientSecret())) {
			parameters = parameters.put("client_secret", credentials.clientSecret());
		}

		return parameters;
	}

	private Header authorization() {
		ClientCredentials credentials = request.credentials();
		BasicAuthentication basic = new BasicAuthentication(credentials.clientId(), credentials.clientSecret());
		return new Header(AUTHORIZATION, basic.content(null));
	}
}