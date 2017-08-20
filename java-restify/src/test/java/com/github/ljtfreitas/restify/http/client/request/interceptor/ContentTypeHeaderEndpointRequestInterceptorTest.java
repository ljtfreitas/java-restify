package com.github.ljtfreitas.restify.http.client.request.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

@RunWith(MockitoJUnitRunner.class)
public class ContentTypeHeaderEndpointRequestInterceptorTest {

	private ContentTypeHeaderEndpointRequestInterceptor contentTypeHeaderEndpointRequestInterceptor;

	private Headers headers;

	@Before
	public void setup() throws Exception {
		contentTypeHeaderEndpointRequestInterceptor = new ContentTypeHeaderEndpointRequestInterceptor("application/json");

		headers = new Headers();
	}

	@Test
	public void shouldAddContentTypeHeaderWhenRequestHasBodyButContentTypeIsNotPresent() throws Exception {
		String body = "{\"field\":\"value\"}";

		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/object"), "GET", headers, body);

		EndpointRequest endpointRequestExpected = contentTypeHeaderEndpointRequestInterceptor
				.intercepts(endpointRequest);

		Optional<Header> contentType = endpointRequestExpected.headers().get("Content-Type");

		assertTrue(contentType.isPresent());
		assertEquals("application/json", contentType.get().value());
	}

	@Test
	public void shouldNotAddContentTypeHeaderWhenRequestHasNoBody() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/object"), "GET");

		EndpointRequest endpointRequestExpected = contentTypeHeaderEndpointRequestInterceptor.intercepts(endpointRequest);

		Optional<Header> contentType = endpointRequestExpected.headers().get("Content-Type");

		assertFalse(contentType.isPresent());
	}

	@Test
	public void shouldNotAddContentTypeHeaderWhenRequestHasBodyAndContentTypeAlreadyIsPresent() throws Exception {
		String body = "<field>value</field>";

		headers.add(Headers.CONTENT_TYPE, "application/xml");

		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/object"), "GET", headers, body);

		EndpointRequest endpointRequestExpected = contentTypeHeaderEndpointRequestInterceptor.intercepts(endpointRequest);

		Optional<Header> contentType = endpointRequestExpected.headers().get("Content-Type");

		assertTrue(contentType.isPresent());
		assertEquals("application/xml", contentType.get().value());
	}
}
