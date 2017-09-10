package com.github.ljtfreitas.restify.http.client.request.interceptor;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.header.Header;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.contract.ContentType;

public class HeaderEndpointRequestInterceptorTest {

	@Test
	public void shouldAddHeadersToRequest() {
		HeaderEndpointRequestInterceptor interceptor = new HeaderEndpointRequestInterceptor(Header.of("X-Bla", "bla"),
				Header.contentType(ContentType.of("application/json")));
		
		EndpointRequest output = interceptor.intercepts(new EndpointRequest(URI.create("http://my.api.com"), "GET"));
		
		assertTrue(output.headers().get("X-Bla").isPresent());
		assertEquals("bla", output.headers().get("X-Bla").get().value());

		assertTrue(output.headers().get("Content-Type").isPresent());
		assertEquals("application/json", output.headers().get("Content-Type").get().value());
	}

}
