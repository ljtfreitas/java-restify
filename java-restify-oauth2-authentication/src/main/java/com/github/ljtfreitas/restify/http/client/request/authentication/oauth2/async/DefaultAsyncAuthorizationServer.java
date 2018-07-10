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
package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.async;

import java.util.concurrent.CompletionStage;

import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenEndpointRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenResponse;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenResponseBody;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCodeEndpointRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCodeRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCodeResponse;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationServerHttpClientFactory;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientAuthenticationMethod;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

class DefaultAsyncAuthorizationServer implements AsyncAuthorizationServer {

	private final AsyncEndpointRequestExecutor delegate;
	private final ClientAuthenticationMethod clientAuthenticationMethod;

	public DefaultAsyncAuthorizationServer() {
		this(new AuthorizationServerHttpClientFactory().createAsync());
	}

	public DefaultAsyncAuthorizationServer(AsyncEndpointRequestExecutor asyncEndpointRequestExecutor) {
		this(asyncEndpointRequestExecutor, ClientAuthenticationMethod.HEADER);
	}

	public DefaultAsyncAuthorizationServer(ClientAuthenticationMethod clientAuthenticationMethod) {
		this(new AuthorizationServerHttpClientFactory().createAsync(), clientAuthenticationMethod);
	}

	public DefaultAsyncAuthorizationServer(HttpMessageConverters converters) {
		this(new AuthorizationServerHttpClientFactory(converters).createAsync());
	}

	public DefaultAsyncAuthorizationServer(HttpMessageConverters converters, ClientAuthenticationMethod clientAuthenticationMethod) {
		this(new AuthorizationServerHttpClientFactory(converters).createAsync(), clientAuthenticationMethod);
	}

	public DefaultAsyncAuthorizationServer(AsyncHttpClientRequestFactory asyncHttpClientRequestFactory) {
		this(new AuthorizationServerHttpClientFactory(asyncHttpClientRequestFactory).createAsync());
	}

	public DefaultAsyncAuthorizationServer(AsyncHttpClientRequestFactory asyncHttpClientRequestFactory, ClientAuthenticationMethod clientAuthenticationMethod) {
		this(new AuthorizationServerHttpClientFactory(asyncHttpClientRequestFactory).createAsync(), clientAuthenticationMethod);
	}

	public DefaultAsyncAuthorizationServer(AsyncEndpointRequestExecutor asyncEndpointRequestExecutor, ClientAuthenticationMethod clientAuthenticationMethod) {
		this.delegate = asyncEndpointRequestExecutor;
		this.clientAuthenticationMethod = clientAuthenticationMethod;
	}

	@Override
	public CompletionStage<AuthorizationCodeResponse> authorize(CompletionStage<AuthorizationCodeRequest> request) {
		return request.thenCompose(this::doAuthorize).thenApply(AuthorizationCodeResponse::new);
	}

	private CompletionStage<EndpointResponse<String>> doAuthorize(AuthorizationCodeRequest request) {
		EndpointRequest authorizationCodeEndpointRequest = new AuthorizationCodeEndpointRequestFactory(request).create();

		return delegate.executeAsync(authorizationCodeEndpointRequest);
	}

	@Override
	public CompletionStage<AccessTokenResponse> requireToken(CompletionStage<AccessTokenRequest> request) {
		return request.thenCompose(this::doRequireToken).thenApply(AccessTokenResponse::new);
	}

	private CompletionStage<EndpointResponse<AccessTokenResponseBody>> doRequireToken(AccessTokenRequest request) {
		EndpointRequest accessTokenEndpointRequest = new AccessTokenEndpointRequestFactory(request, clientAuthenticationMethod).create();

		return delegate.executeAsync(accessTokenEndpointRequest);
	}
}
