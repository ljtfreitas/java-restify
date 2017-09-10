package com.github.ljtfreitas.restify.http.client.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.header.Headers;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;

public class EndpointRequestTest {

	@Test
	public void shouldAppendQueryParameterOnEndpointRequest() throws URISyntaxException {
		URI endpoint = new URI("http://my.api.com/path/to/my/api");

		EndpointRequest endpointRequest = new EndpointRequest(endpoint, "GET", new Headers(), "body", String.class);

		EndpointRequest newEndpointRequest = endpointRequest.appendParameter("param1", "value1");

		assertEquals(new URI("http://my.api.com/path/to/my/api?param1=value1"), newEndpointRequest.endpoint());
		assertEquals(endpointRequest.method(), newEndpointRequest.method());

		assertSame(endpointRequest.body().get(), newEndpointRequest.body().get());
		assertSame(endpointRequest.headers(), newEndpointRequest.headers());
		assertSame(endpointRequest.responseType(), newEndpointRequest.responseType());
	}

	@Test
	public void shouldAppendQueryParameterOnEndpointRequestWhenURIAlreadyContainsParameters()
			throws URISyntaxException {
		URI endpoint = new URI("http://my.api.com/path/to/my/api?param1=value1&param2=value2");

		EndpointRequest endpointRequest = new EndpointRequest(endpoint, "GET", new Headers(), "body", String.class);

		EndpointRequest newEndpointRequest = endpointRequest.appendParameter("param3", "value3");

		assertEquals(new URI("http://my.api.com/path/to/my/api?param1=value1&param2=value2&param3=value3"),
				newEndpointRequest.endpoint());
		assertEquals(endpointRequest.method(), newEndpointRequest.method());

		assertSame(endpointRequest.body().get(), newEndpointRequest.body().get());
		assertSame(endpointRequest.headers(), newEndpointRequest.headers());
		assertSame(endpointRequest.responseType(), newEndpointRequest.responseType());
	}
}
