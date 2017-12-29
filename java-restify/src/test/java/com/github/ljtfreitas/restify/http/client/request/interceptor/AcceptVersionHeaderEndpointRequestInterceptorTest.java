package com.github.ljtfreitas.restify.http.client.request.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointVersion;

public class AcceptVersionHeaderEndpointRequestInterceptorTest {

	private AcceptVersionHeaderEndpointRequestInterceptor interceptor;

	@Before
	public void setup() {
		interceptor = new AcceptVersionHeaderEndpointRequestInterceptor();
	}

	@Test
	public void shouldAddAcceptVersionHeaderWhenVersionIsPresentOnRequest() {
		EndpointRequest request = new EndpointRequest(URI.create("http://my.api.com"), "GET", EndpointVersion.of("v1"));

		EndpointRequest newRequest = interceptor.intercepts(request);

		Optional<Header> acceptVersion = newRequest.headers().get("Accept-Version");

		assertTrue(acceptVersion.isPresent());
		assertEquals("v1", acceptVersion.get().value());
	}

	@Test
	public void shouldNotAddAcceptVersionHeaderWhenVersionIsNotPresentOnRequest() {
		EndpointRequest request = new EndpointRequest(URI.create("http://my.api.com"), "GET");

		EndpointRequest newRequest = interceptor.intercepts(request);

		Optional<Header> acceptVersion = newRequest.headers().get("Accept-Version");

		assertFalse(acceptVersion.isPresent());
	}

	@Test
	public void shouldOverrideRequestVersionWhenAcceptHeaderVersionIsConfigured() {
		EndpointRequest request = new EndpointRequest(URI.create("http://my.api.com"), "GET", EndpointVersion.of("v1"));

		interceptor = new AcceptVersionHeaderEndpointRequestInterceptor(EndpointVersion.of("v2"));

		EndpointRequest newRequest = interceptor.intercepts(request);

		Optional<Header> acceptVersion = newRequest.headers().get("Accept-Version");

		assertTrue(acceptVersion.isPresent());
		assertEquals("v2", acceptVersion.get().value());
	}
}
