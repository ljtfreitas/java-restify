package com.restify.http.client.request;

public interface HttpClientRequestFactory {

	public HttpClientRequest createOf(EndpointRequest endpointRequest);
}
