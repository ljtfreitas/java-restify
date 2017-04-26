package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

public class OAuth2Exception extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OAuth2Exception(String message, Throwable cause) {
		super(message, cause);
	}

	public OAuth2Exception(String message) {
		super(message);
	}

	public OAuth2Exception(Throwable cause) {
		super(cause);
	}
}
