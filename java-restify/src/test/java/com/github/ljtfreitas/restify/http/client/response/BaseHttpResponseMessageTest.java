package com.github.ljtfreitas.restify.http.client.response;

import static com.github.ljtfreitas.restify.http.client.message.Headers.CONTENT_LENGTH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.BaseHttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.SimpleHttpRequestMessage;

public class BaseHttpResponseMessageTest {

	private HttpRequestMessage httpRequestMessage;

	@Before
	public void setup() {
		httpRequestMessage = new SimpleHttpRequestMessage(new EndpointRequest(URI.create("http://any.api"), "GET"));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsSuccess() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.ok(), httpRequestMessage);
		assertTrue(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsServerError() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.internalServerError(), httpRequestMessage);
		assertTrue(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsClientError() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.notFound(), httpRequestMessage);
		assertTrue(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsNoContent() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.noContent(), httpRequestMessage);
		assertFalse(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsInformational() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.of(HttpStatusCode.CONTINUE), httpRequestMessage);
		assertFalse(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsNotModified() {
		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.notModified(), httpRequestMessage);
		assertFalse(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenContentLengthIsZero() {
		Headers headers = new Headers(new Header(CONTENT_LENGTH, "0"));

		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.ok(), headers, httpRequestMessage);
		assertFalse(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenHttpRequestMethodIsHead() {
		httpRequestMessage = new SimpleHttpRequestMessage(new EndpointRequest(URI.create("http://any.api"), "HEAD"));

		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.ok(), httpRequestMessage);
		assertFalse(httpResponseMessage.readable());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenHttpRequestMethodIsTrace() {
		httpRequestMessage = new SimpleHttpRequestMessage(new EndpointRequest(URI.create("http://any.api"), "TRACE"));

		BaseHttpResponseMessage httpResponseMessage = new StubHttpResponseMessage(StatusCode.ok(), httpRequestMessage);
		assertFalse(httpResponseMessage.readable());
	}

	private class StubHttpResponseMessage extends BaseHttpResponseMessage {

		public StubHttpResponseMessage(StatusCode statusCode, HttpRequestMessage source) {
			super(statusCode, new Headers(), null, source);
		}

		public StubHttpResponseMessage(StatusCode statusCode, Headers headers, HttpRequestMessage source) {
			super(statusCode, headers, null, source);
		}

		@Override
		public void close() throws IOException {
		}
	}

}
