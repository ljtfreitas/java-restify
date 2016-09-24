package com.restify.http.client.response;

import static com.restify.http.client.Headers.CONTENT_LENGTH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.restify.http.client.Header;
import com.restify.http.client.Headers;

public class BaseHttpResponseMessageTest {

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsSuccess() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(EndpointResponseCode.ok());
		assertTrue(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsServerError() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(EndpointResponseCode.internalServerError());
		assertTrue(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsClientError() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(EndpointResponseCode.notFound());
		assertTrue(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsNoContent() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(EndpointResponseCode.noContent());
		assertFalse(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsInformational() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(EndpointResponseCode.contine());
		assertFalse(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsNotModified() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(EndpointResponseCode.notModified());
		assertFalse(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenContentLengthIsZero() {
		Headers headers = new Headers(new Header(CONTENT_LENGTH, "0"));

		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(EndpointResponseCode.ok(), headers);
		assertFalse(httpResponseMessage.readable());
	}

	private class StubHttpResponseMessage extends BaseHttpResponseMessage {

		public StubHttpResponseMessage(EndpointResponseCode code) {
			super(code, new Headers(), null);
		}

		public StubHttpResponseMessage(EndpointResponseCode code, Headers headers) {
			super(code, headers, null);
		}

		@Override
		public void close() throws IOException {
		}
	}

}
