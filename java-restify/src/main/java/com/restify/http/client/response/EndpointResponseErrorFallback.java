package com.restify.http.client.response;

public interface EndpointResponseErrorFallback {

	public <T> EndpointResponse<T> onError(HttpResponseMessage response);
}
