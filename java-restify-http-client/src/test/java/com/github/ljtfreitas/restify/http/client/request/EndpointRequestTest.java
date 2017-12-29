package com.github.ljtfreitas.restify.http.client.request;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.net.URI;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.contract.Parameters;

public class EndpointRequestTest {

	@Test
	public void shouldAppendQueryParameterOnEndpointRequest() throws Exception {
		URI endpoint = new URI("http://my.api.com/path/to/my/api");

		EndpointRequest endpointRequest = new EndpointRequest(endpoint, "GET", new Headers(), "body", String.class);

		EndpointRequest newEndpointRequest = endpointRequest.append(Parameters.parse("param1=value1"));

		assertEquals(new URI("http://my.api.com/path/to/my/api?param1=value1"), newEndpointRequest.endpoint());
		assertEquals(endpointRequest.method(), newEndpointRequest.method());

		assertSame(endpointRequest.body().get(), newEndpointRequest.body().get());
		assertSame(endpointRequest.headers(), newEndpointRequest.headers());
		assertSame(endpointRequest.responseType(), newEndpointRequest.responseType());
	}

	@Test
	public void shouldAppendQueryParameterOnEndpointRequestWhenURIAlreadyContainsParameters()
			throws Exception {
		URI endpoint = new URI("http://my.api.com/path/to/my/api?param1=value1&param2=value2");

		EndpointRequest endpointRequest = new EndpointRequest(endpoint, "GET", new Headers(), "body", String.class);

		EndpointRequest newEndpointRequest = endpointRequest.append(Parameters.parse("param3=value3"));

		assertEquals(new URI("http://my.api.com/path/to/my/api?param1=value1&param2=value2&param3=value3"),
				newEndpointRequest.endpoint());
		assertEquals(endpointRequest.method(), newEndpointRequest.method());

		assertSame(endpointRequest.body().get(), newEndpointRequest.body().get());
		assertSame(endpointRequest.headers(), newEndpointRequest.headers());
		assertSame(endpointRequest.responseType(), newEndpointRequest.responseType());
	}

	@Test
	public void shouldAddNewHeaderOnEndpointRequest() throws Exception {
		URI endpoint = new URI("http://my.api.com/path/to/my/api?param1=value1&param2=value2");

		EndpointRequest endpointRequest = new EndpointRequest(endpoint, "GET", new Headers(), "body", String.class);

		Header newHeader = Header.of("X-Custom", "sample");

		EndpointRequest newEndpointRequest = endpointRequest.add(newHeader);

		assertThat(endpointRequest.headers(), not(hasItem(newHeader)));
		assertThat(newEndpointRequest.headers(), hasItem(newHeader));

		assertEquals(endpointRequest.endpoint(), newEndpointRequest.endpoint());
		assertEquals(endpointRequest.method(), newEndpointRequest.method());
		assertSame(endpointRequest.body().get(), newEndpointRequest.body().get());
		assertSame(endpointRequest.responseType(), newEndpointRequest.responseType());
	}
}
