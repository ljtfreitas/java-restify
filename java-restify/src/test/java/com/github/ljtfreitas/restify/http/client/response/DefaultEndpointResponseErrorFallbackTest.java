package com.github.ljtfreitas.restify.http.client.response;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.util.function.Function;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.ljtfreitas.restify.http.client.Headers;

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
		expectedException.expectMessage(allOf(startsWith("HTTP Status Code: " + response.statusCode()), endsWith(body)));

		expectedException.expect(method(e -> e.statusCode(), is(response.statusCode())));
		expectedException.expect(method(e -> e.headers(), sameInstance(response.headers())));
		expectedException.expect(method(e -> e.bodyAsString(), is(body)));

		fallback.onError(response, null);
	}

	@Test
	public void shouldThrowExceptionWhenHttpResponseIsNotFoundButNotMustReturnEmpty() {
		String body = "http response body";

		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.notFound(), new Headers(), new ByteArrayInputStream(body.getBytes()));

		expectedException.expect(RestifyEndpointResponseException.class);
		expectedException.expectMessage(allOf(startsWith("HTTP Status Code: " + response.statusCode()), endsWith(body)));

		expectedException.expect(method(e -> e.statusCode(), is(response.statusCode())));
		expectedException.expect(method(e -> e.headers(), sameInstance(response.headers())));
		expectedException.expect(method(e -> e.bodyAsString(), is(body)));

		fallback.onError(response, null);
	}

	@Test
	public void shouldReturnEmptyEndpointResponseWhenHttpResponseIsNotFoundAndEmptyOnNotFoundIsTrue() {
		String body = "http response body";

		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.notFound(), new Headers(), new ByteArrayInputStream(body.getBytes()));

		fallback = DefaultEndpointResponseErrorFallback.emptyOnNotFound();

		EndpointResponse<Object> newEndpointResponse = fallback.onError(response, null);

		assertEquals(response.statusCode(), newEndpointResponse.code());
		assertSame(response.headers(), newEndpointResponse.headers());
		assertNull(newEndpointResponse.body());
	}

	private <T> FeatureMatcher<RestifyEndpointResponseException, T> method(Function<RestifyEndpointResponseException, T> function, Matcher<T> matcher) {
		return new FeatureMatcher<RestifyEndpointResponseException, T>(matcher, "method", "value") {
			@Override
			protected T featureValueOf(RestifyEndpointResponseException actual) {
				return function.apply(actual);
			}
		};
	}
}
