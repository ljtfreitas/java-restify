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

import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

class DefaultAuthorizationServer implements AuthorizationServer {

	private final EndpointRequestExecutor delegate;
	private final ClientAuthenticationMethod clientAuthenticationMethod;

	public DefaultAuthorizationServer() {
		this(new AuthorizationServerHttpClientFactory().create());
	}

	public DefaultAuthorizationServer(EndpointRequestExecutor endpointRequestExecutor) {
		this(endpointRequestExecutor, ClientAuthenticationMethod.HEADER);
	}

	public DefaultAuthorizationServer(ClientAuthenticationMethod clientAuthenticationMethod) {
		this(new AuthorizationServerHttpClientFactory().create(), clientAuthenticationMethod);
	}

	public DefaultAuthorizationServer(HttpMessageConverters converters) {
		this(new AuthorizationServerHttpClientFactory(converters).create());
	}

	public DefaultAuthorizationServer(HttpMessageConverters converters, ClientAuthenticationMethod clientAuthenticationMethod) {
		this(new AuthorizationServerHttpClientFactory(converters).create(), clientAuthenticationMethod);
	}

	public DefaultAuthorizationServer(HttpClientRequestFactory httpClientRequestFactory) {
		this(new AuthorizationServerHttpClientFactory(httpClientRequestFactory).create());
	}

	public DefaultAuthorizationServer(HttpClientRequestFactory httpClientRequestFactory, ClientAuthenticationMethod clientAuthenticationMethod) {
		this(new AuthorizationServerHttpClientFactory(httpClientRequestFactory).create(), clientAuthenticationMethod);
	}

	public DefaultAuthorizationServer(EndpointRequestExecutor endpointRequestExecutor, ClientAuthenticationMethod clientAuthenticationMethod) {
		this.delegate = endpointRequestExecutor;
		this.clientAuthenticationMethod = clientAuthenticationMethod;
	}

	@Override
	public AuthorizationCodeResponse authorize(AuthorizationCodeRequest request) {
		EndpointRequest authorizationCodeEndpointRequest = new AuthorizationCodeEndpointRequestFactory(request).create();

		EndpointResponse<String> response = delegate.execute(authorizationCodeEndpointRequest);

		return new AuthorizationCodeResponse(response);
	}

	@Override
	public AccessTokenResponse requireToken(AccessTokenRequest request) {
		EndpointRequest accessTokenEndpointRequest = new AccessTokenEndpointRequestFactory(request, clientAuthenticationMethod).create();

		EndpointResponse<AccessTokenResponseBody> accessTokenResponse = delegate.execute(accessTokenEndpointRequest);

		return new AccessTokenResponse(accessTokenResponse);
	}
}
