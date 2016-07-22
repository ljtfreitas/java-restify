package com.restify.http.client;

public interface HttpClientRequestFactory {

	public HttpClientRequest createOf(EndpointRequest endpointRequest);
}
