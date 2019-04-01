package com.github.ljtfreitas.restify.http.client.response;

import static com.github.ljtfreitas.restify.http.client.message.Headers.CONTENT_LENGTH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;

@RunWith(MockitoJUnitRunner.class)
public class BaseHttpClientResponseTest {

	@Mock
	private HttpRequestMessage httpRequestMessage;

	@Before
	public void setup() {
		when(httpRequestMessage.method()).thenReturn("GET");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsSuccess() {
		BaseHttpClientResponse httpClientResponse = new StubHttpClientResponse(StatusCode.ok(), httpRequestMessage);
		assertTrue(httpClientResponse.available());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsServerError() {
		BaseHttpClientResponse httpClientResponse = new StubHttpClientResponse(StatusCode.internalServerError(), httpRequestMessage);
		assertTrue(httpClientResponse.available());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBeReadableWhenStatusCodeIsClientError() {
		BaseHttpClientResponse httpClientResponse = new StubHttpClientResponse(StatusCode.notFound(), httpRequestMessage);
		assertTrue(httpClientResponse.available());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsNoContent() {
		BaseHttpClientResponse httpClientResponse = new StubHttpClientResponse(StatusCode.noContent(), httpRequestMessage);
		assertFalse(httpClientResponse.available());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsInformational() {
		BaseHttpClientResponse httpClientResponse = new StubHttpClientResponse(StatusCode.of(HttpStatusCode.CONTINUE), httpRequestMessage);
		assertFalse(httpClientResponse.available());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenStatusCodeIsNotModified() {
		BaseHttpClientResponse httpClientResponse = new StubHttpClientResponse(StatusCode.notModified(), httpRequestMessage);
		assertFalse(httpClientResponse.available());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenContentLengthIsZero() {
		Headers headers = new Headers(new Header(CONTENT_LENGTH, "0"));

		BaseHttpClientResponse httpClientResponse = new StubHttpClientResponse(StatusCode.ok(), headers, httpRequestMessage);
		assertFalse(httpClientResponse.available());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenHttpRequestMethodIsHead() {
		when(httpRequestMessage.method()).thenReturn("HEAD");

		BaseHttpClientResponse httpClientResponse = new StubHttpClientResponse(StatusCode.ok(), httpRequestMessage);
		assertFalse(httpClientResponse.available());
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotBeReadableWhenHttpRequestMethodIsTrace() {
		when(httpRequestMessage.method()).thenReturn("TRACE");

		BaseHttpClientResponse httpClientResponse = new StubHttpClientResponse(StatusCode.ok(), httpRequestMessage);
		assertFalse(httpClientResponse.available());
	}

	private class StubHttpClientResponse extends BaseHttpClientResponse {

		public StubHttpClientResponse(StatusCode statusCode, HttpRequestMessage source) {
			super(statusCode, new Headers(), null, source);
		}

		public StubHttpClientResponse(StatusCode statusCode, Headers headers, HttpRequestMessage source) {
			super(statusCode, headers, null, source);
		}

		@Override
		public void close() throws IOException {
		}
	}

}
