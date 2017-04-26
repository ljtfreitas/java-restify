package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.jdk.JdkHttpClientRequestFactory;

public class DefaultAuthorizationCodeAccessTokenProvider implements OAuth2AccessTokenProvider {

	private final OAuth2AuthorizationConfiguration configuration;

	public DefaultAuthorizationCodeAccessTokenProvider(OAuth2AuthorizationConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public OAuth2AccessToken get() {
		String authorizationCode = obtainAuthorizationCode();

		return null;
	}

	private String obtainAuthorizationCode() {
		HttpClientRequestFactory httpClientRequestFactory = new JdkHttpClientRequestFactory();

		EndpointRequest authorizationCodeRequest = buildAuthorizationRequest();

		return null;
	}

	protected EndpointRequest buildAuthorizationRequest() {
		return null;
	}
}
