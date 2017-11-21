package com.github.ljtfreitas.restify.http.client.response;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.function.Function;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEndpointResponseErrorFallbackTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock(answer = Answers.RETURNS_MOCKS)
	private HttpResponseMessage response;

	private DefaultEndpointResponseErrorFallback fallback;

	private String body;

	@Before
	public void setup() {
		fallback = new DefaultEndpointResponseErrorFallback();
		
		body = "http response body";
		
		when(response.body()).thenReturn(new ByteArrayInputStream(body.getBytes()));
		when(response.headers()).thenReturn(new Headers());
	}

	@Test
	public void shouldThrowExceptionWithHttpResponseBody() {
		when(response.status()).thenReturn(StatusCode.internalServerError());

		expectedException.expect(EndpointResponseException.class);
		expectedException.expectMessage(allOf(containsString(response.status().toString()), endsWith(body)));

		expectedException.expect(method(e -> e.status(), is(response.status())));
		expectedException.expect(method(e -> e.headers(), sameInstance(response.headers())));
		expectedException.expect(method(e -> e.bodyAsString(), is(body)));

		fallback.onError(response, null);
	}

	@Test
	public void shouldThrowExceptionWhenHttpResponseIsNotFoundButNotMustReturnEmpty() {
		when(response.status()).thenReturn(StatusCode.notFound());

		expectedException.expect(EndpointResponseException.class);
		expectedException.expectMessage(allOf(containsString(response.status().toString()), endsWith(body)));

		expectedException.expect(method(e -> e.status(), is(response.status())));
		expectedException.expect(method(e -> e.headers(), sameInstance(response.headers())));
		expectedException.expect(method(e -> e.bodyAsString(), is(body)));

		fallback.onError(response, null);
	}

	@Test
	public void shouldReturnEmptyEndpointResponseWhenHttpResponseIsNotFoundAndEmptyOnNotFoundIsTrue() {
		when(response.status()).thenReturn(StatusCode.notFound());

		fallback = DefaultEndpointResponseErrorFallback.emptyOnNotFound();

		EndpointResponse<Object> newEndpointResponse = fallback.onError(response, null);

		assertEquals(response.status(), newEndpointResponse.status());
		assertSame(response.headers(), newEndpointResponse.headers());
		assertNull(newEndpointResponse.body());
	}

	private <T> FeatureMatcher<EndpointResponseException, T> method(Function<EndpointResponseException, T> function, Matcher<T> matcher) {
		return new FeatureMatcher<EndpointResponseException, T>(matcher, "method", "value") {
			@Override
			protected T featureValueOf(EndpointResponseException actual) {
				return function.apply(actual);
			}
		};
	}
}
