package com.github.ljtfreitas.restify.http.client.response;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.response.DefaultEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseException;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;

public class DefaultEndpointResponseErrorFallbackTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private DefaultEndpointResponseErrorFallback fallback;

	@Before
	public void setup() {
		fallback = new DefaultEndpointResponseErrorFallback();
	}

	@Test
	public void shouldThrowExceptionWithHttpResponseBody() {
		String body = "http response body";

		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.internalServerError(), new Headers(), new ByteArrayInputStream(body.getBytes()));

		expectedException.expect(RestifyEndpointResponseException.class);
		expectedException.expectMessage(allOf(startsWith("HTTP Status Code: " + response.code()), endsWith(body)));

		expectedException.expect(hasProperty("statusCode", is(response.code())));
		expectedException.expect(hasProperty("headers", sameInstance(response.headers())));
		expectedException.expect(hasProperty("body", is(body)));

		fallback.onError(response);
	}

	@Test
	public void shouldThrowExceptionWhenHttpResponseIsNotFoundButNotMustReturnEmpty() {
		String body = "http response body";

		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.notFound(), new Headers(), new ByteArrayInputStream(body.getBytes()));

		expectedException.expect(RestifyEndpointResponseException.class);
		expectedException.expectMessage(allOf(startsWith("HTTP Status Code: " + response.code()), endsWith(body)));

		expectedException.expect(hasProperty("statusCode", is(response.code())));
		expectedException.expect(hasProperty("headers", sameInstance(response.headers())));
		expectedException.expect(hasProperty("body", is(body)));

		fallback.onError(response);
	}

	@Test
	public void shouldReturnEmptyEndpointResponseWhenHttpResponseIsNotFoundAndEmptyOnNotFoundIsTrue() {
		String body = "http response body";

		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.notFound(), new Headers(), new ByteArrayInputStream(body.getBytes()));

		fallback = DefaultEndpointResponseErrorFallback.emptyOnNotFound();

		EndpointResponse<Object> newEndpointResponse = fallback.onError(response);

		assertEquals(response.code(), newEndpointResponse.code());
		assertSame(response.headers(), newEndpointResponse.headers());
		assertNull(newEndpointResponse.body());
	}
}
