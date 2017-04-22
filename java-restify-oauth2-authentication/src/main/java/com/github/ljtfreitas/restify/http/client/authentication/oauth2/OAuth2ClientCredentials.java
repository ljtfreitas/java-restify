package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

public class OAuth2ClientCredentials {

	private final String clientId;
	private final String clientSecret;

	public OAuth2ClientCredentials(String clientId, String clientSecret) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	public String clientId() {
		return clientId;
	}

	public String clientSecret() {
		return clientSecret;
	}

	public static OAuth2ClientCredentials clientId(String clientId) {
		return new OAuth2ClientCredentials(clientId, null);
}
}
