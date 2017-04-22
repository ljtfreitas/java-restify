package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import java.util.Arrays;

public enum OAuth2AccessTokenType {
	BEARER("Bearer"), OAUTH("OAuth");

	private final String name;

	private OAuth2AccessTokenType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static OAuth2AccessTokenType of(String tokenType) {
		return Arrays.stream(values()).filter(t -> t.name.equalsIgnoreCase(tokenType)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unsupported OAuth token type: " + tokenType));
	}
}
