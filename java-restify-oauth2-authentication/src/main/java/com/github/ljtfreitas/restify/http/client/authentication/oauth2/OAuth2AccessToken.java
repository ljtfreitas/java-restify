package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

public class OAuth2AccessToken {

	private final OAuth2AccessTokenType tokenType;
	private final String token;

	public OAuth2AccessToken(OAuth2AccessTokenType tokenType, String token) {
		this.tokenType = tokenType;
		this.token = token;
	}

	@Override
	public String toString() {
		return tokenType + " " + token;
	}

	public static OAuth2AccessToken bearer(String token) {
		return new OAuth2AccessToken(OAuth2AccessTokenType.BEARER, token);
	}
}
