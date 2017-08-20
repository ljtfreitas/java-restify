package com.github.ljtfreitas.restify.http.client.request.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.header.Header;
import com.github.ljtfreitas.restify.http.client.header.Headers;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.interceptor.AcceptHeaderEndpointRequestInterceptor;

@RunWith(MockitoJUnitRunner.class)
public class AcceptHeaderEndpointRequestInterceptorTest {

	private AcceptHeaderEndpointRequestInterceptor acceptHeaderEndpointRequestInterceptor;

	@Before
	public void setup() throws Exception {
		acceptHeaderEndpointRequestInterceptor = new AcceptHeaderEndpointRequestInterceptor("text/plain", "application/json", "application/xml");
	}

	@Test
	public void shouldBuildAcceptHeaderWithAllSupportedContentTypes() throws Exception {
		EndpointRequest endpointRequestObjectExpected = acceptHeaderEndpointRequestInterceptor
				.intercepts(new EndpointRequest(new URI("http://my.api.com/object"), "GET", Object.class));

		Optional<Header> acceptHeader = endpointRequestObjectExpected.headers().get("Accept");

		assertTrue(acceptHeader.isPresent());
		assertEquals("text/plain, application/json, application/xml", acceptHeader.get().value());
	}

	@Test
	public void shouldNotCreateAcceptHeaderWhenAlreadyExists() throws Exception {
		Header accept = new Header("Accept", "text/plain");

		Headers headers = new Headers(accept);

		EndpointRequest endpointRequest = acceptHeaderEndpointRequestInterceptor
				.intercepts(new EndpointRequest(new URI("http://my.api.com/object"), "GET", headers, Object.class));

		assertSame(accept, endpointRequest.headers().get("Accept").get());
	}
}
