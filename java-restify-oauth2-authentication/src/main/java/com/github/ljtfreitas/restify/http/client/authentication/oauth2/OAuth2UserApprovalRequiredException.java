package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

public class OAuth2UserApprovalRequiredException extends OAuth2Exception {

	private static final long serialVersionUID = 1L;

	public OAuth2UserApprovalRequiredException(String message, Throwable cause) {
		super(message, cause);
	}

	public OAuth2UserApprovalRequiredException(String message) {
		super(message);
	}

	public OAuth2UserApprovalRequiredException(Throwable cause) {
		super(cause);
	}
}
