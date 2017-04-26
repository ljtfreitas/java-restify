package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseExceptionFactory;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;

public class OAuth2EndpointResponseErrorFallback implements EndpointResponseErrorFallback {

	private final EndpointResponseExceptionFactory endpointResponseExceptionFactory = new EndpointResponseExceptionFactory();

	@Override
	public <T> EndpointResponse<T> onError(HttpResponseMessage response, JavaType responseType) {
		throw new OAuth2Exception("OAuth response error", endpointResponseExceptionFactory.create(response));
	}
}
