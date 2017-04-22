package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import com.github.ljtfreitas.restify.http.client.authentication.Authentication;

public class OAuth2Authentication implements Authentication {

	private final OAuth2AccessTokenProvider accessTokenProvider;

	public OAuth2Authentication(OAuth2AccessTokenProvider accessTokenProvider) {
		this.accessTokenProvider = accessTokenProvider;
	}

	@Override
	public String content() {
		OAuth2AccessToken accessToken = accessTokenProvider.get();
		return accessToken.toString();
	}
}
