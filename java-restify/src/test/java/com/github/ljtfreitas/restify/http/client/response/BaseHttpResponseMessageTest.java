package com.github.ljtfreitas.restify.http.client.response;

import static com.github.ljtfreitas.restify.http.client.Headers.CONTENT_LENGTH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.Header;
import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.response.BaseHttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;

public class BaseHttpResponseMessageTest {

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsSuccess() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.ok());
		assertTrue(httpResponseMessage.isReadable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsServerError() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.internalServerError());
		assertTrue(httpResponseMessage.isReadable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsClientError() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.notFound());
		assertTrue(httpResponseMessage.isReadable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsNoContent() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.noContent());
		assertFalse(httpResponseMessage.isReadable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsInformational() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.contine());
		assertFalse(httpResponseMessage.isReadable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsNotModified() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.notModified());
		assertFalse(httpResponseMessage.isReadable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenContentLengthIsZero() {
		Headers headers = new Headers(new Header(CONTENT_LENGTH, "0"));

		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.ok(), headers);
		assertFalse(httpResponseMessage.isReadable());
	}

	private class StubHttpResponseMessage extends BaseHttpResponseMessage {

		public StubHttpResponseMessage(StatusCode statusCode) {
			super(statusCode, new Headers(), null);
		}

		public StubHttpResponseMessage(StatusCode statusCode, Headers headers) {
			super(statusCode, headers, null);
		}

		@Override
		public void close() throws IOException {
		}
	}

}
