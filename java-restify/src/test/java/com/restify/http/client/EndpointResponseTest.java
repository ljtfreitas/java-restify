package com.restify.http.client;

import static com.restify.http.client.Headers.CONTENT_LENGTH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class EndpointResponseTest {

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsSuccess() {
		EndpointResponse endpointResponse = new SimpleEndpointResponse(EndpointResponseCode.ok());
		assertTrue(endpointResponse.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsServerError() {
		EndpointResponse endpointResponse = new SimpleEndpointResponse(EndpointResponseCode.internalServerError());
		assertTrue(endpointResponse.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsClientError() {
		EndpointResponse endpointResponse = new SimpleEndpointResponse(EndpointResponseCode.notFound());
		assertTrue(endpointResponse.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsNoContent() {
		EndpointResponse endpointResponse = new SimpleEndpointResponse(EndpointResponseCode.noContent());
		assertFalse(endpointResponse.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsInformational() {
		EndpointResponse endpointResponse = new SimpleEndpointResponse(EndpointResponseCode.contine());
		assertFalse(endpointResponse.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsNotModified() {
		EndpointResponse endpointResponse = new SimpleEndpointResponse(EndpointResponseCode.notModified());
		assertFalse(endpointResponse.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenContentLengthIsZero() {
		Headers headers = new Headers(new Header(CONTENT_LENGTH, "0"));

		EndpointResponse endpointResponse = new SimpleEndpointResponse(EndpointResponseCode.ok(), headers);

		assertFalse(endpointResponse.readable());
	}

	private class SimpleEndpointResponse extends EndpointResponse {

		public SimpleEndpointResponse(EndpointResponseCode code) {
			super(code, new Headers(), null);
		}

		public SimpleEndpointResponse(EndpointResponseCode code, Headers headers) {
			super(code, headers, null);
		}

		@Override
		public void close() throws IOException {
		}
	}

}
