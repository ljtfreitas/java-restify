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

import java.security.Principal;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.Authentication;

public class OAuth2Authentication implements Authentication {

	private final GrantProperties properties;
	private final AccessTokenRepository accessTokenRepository;
	private final Principal user;

	public OAuth2Authentication(GrantProperties properties, AccessTokenRepository accessTokenRepository) {
		this(properties, accessTokenRepository, null);
	}

	public OAuth2Authentication(GrantProperties properties, AccessTokenProvider accessTokenProvider) {
		this(properties, accessTokenProvider, (Principal) null);
	}

	public OAuth2Authentication(GrantProperties properties, AccessTokenProvider accessTokenProvider,
			Principal user) {
		this(properties, accessTokenProvider, new AccessTokenMemoryStorage(), user);
	}

	public OAuth2Authentication(GrantProperties properties, AccessTokenProvider accessTokenProvider,
			AccessTokenStorage accessTokenStorage) {
		this(properties, accessTokenProvider, accessTokenStorage, null);
	}

	public OAuth2Authentication(GrantProperties properties, AccessTokenProvider accessTokenProvider,
			AccessTokenStorage accessTokenStorage, Principal user) {
		this(properties, new DefaultAccessTokenRepository(accessTokenProvider, accessTokenStorage), null);
	}

	public OAuth2Authentication(GrantProperties properties, AccessTokenRepository accessTokenRepository, Principal user) {
		this.properties = properties;
		this.accessTokenRepository = accessTokenRepository;
		this.user = user;
	}

	@Override
	public String content(EndpointRequest endpointRequest) {
		AccessToken accessToken = accessTokenRepository.findBy(new OAuth2AuthenticatedEndpointRequest(endpointRequest, properties, user));
		return accessToken.toString();
	}
}
