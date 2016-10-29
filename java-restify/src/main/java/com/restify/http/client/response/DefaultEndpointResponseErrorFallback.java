package com.restify.http.client.response;

import com.restify.http.client.message.converter.text.TextPlainMessageConverter;

public class DefaultEndpointResponseErrorFallback implements EndpointResponseErrorFallback {

	private static final TextPlainMessageConverter ERROR_RESPONSE_MESSAGE_CONVERTER = new TextPlainMessageConverter();

	private final boolean emptyOnNotFound;

	public DefaultEndpointResponseErrorFallback() {
		this.emptyOnNotFound = false;
	}

	private DefaultEndpointResponseErrorFallback(boolean emptyOnNotFound) {
		this.emptyOnNotFound = emptyOnNotFound;
	}

	@Override
	public <T> EndpointResponse<T> onError(HttpResponseMessage response) {
		if (response.code().isNotFound() && emptyOnNotFound) {
			return EndpointResponse.empty(response.code(), response.headers());

		} else {
			throw exception(response);
		}
	}

	private RestifyEndpointResponseException exception(HttpResponseMessage response) {
		String body = ERROR_RESPONSE_MESSAGE_CONVERTER.read(response, String.class);

		String message = "HTTP Status Code: " + response.code() + "\n" + body;

		throw new RestifyEndpointResponseException(message, response.code(), response.headers(), body);
	}

	public static DefaultEndpointResponseErrorFallback emptyOnNotFound() {
		return new DefaultEndpointResponseErrorFallback(true);
	}
}
