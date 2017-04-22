package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

public class SimpleOAuth2AccessTokenProvider implements OAuth2AccessTokenProvider {

	private final OAuth2AccessToken accessToken;

	public SimpleOAuth2AccessTokenProvider(OAuth2AccessToken accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public OAuth2AccessToken get() {
		return accessToken;
	}

}
