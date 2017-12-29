package com.github.ljtfreitas.restify.http.client.retry;

import static com.github.ljtfreitas.restify.http.client.retry.RetryCondition.HeadersRetryCondition.contains;
import static com.github.ljtfreitas.restify.http.client.retry.RetryCondition.StatusCodeRetryCondition.any4xx;
import static com.github.ljtfreitas.restify.http.client.retry.RetryCondition.ThrowableRetryCondition.ioFailure;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.SocketException;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseBadRequestException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseGatewayTimeoutException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseInternalServerErrorException;

public class RetryConditionMatcherTest {

	private RetryConditionMatcher matcher;

	@Before
	public void setup() {
		RetryConfiguration configuration = new RetryConfiguration.Builder()
				.when(MyException.class)
				.when(HttpStatusCode.INTERNAL_SERVER_ERROR)
				.when(any4xx())
				.when(ioFailure())
				.when(contains("X-Header-Retry"))
				.when((EndpointResponse<String> response) ->
					response.status().is(429) && response.headers().get("X-Header-Retry-2").isPresent())
				.build();

		matcher = new RetryConditionMatcher(configuration.conditions());
	}

	@Test
	public void shouldBeRetryableWhenThrowableTypeIsConfigured() {
		assertTrue(matcher.match(new MyException()));
	}

	@Test
	public void shouldNotBeRetryableWhenThrowableTypeIsNotConfigured() {
		assertFalse(matcher.match(new IllegalArgumentException("fail...")));
	}

	@Test
	public void shouldBeRetryableWhenThrowableCauseTypeIsConfigured() {
		assertTrue(matcher.match(new Exception(new MyException())));
	}

	@Test
	public void ioExceptionShouldBeRetryableWhenIOFailureIsConfigured() {
		assertTrue(matcher.match(new IOException()));
	}

	@Test
	public void ioExceptionCauseShouldBeRetryableWhenIOFailureIsConfigured() {
		assertTrue(matcher.match(new RuntimeException(new SocketException())));
	}

	@Test
	public void shouldBeRetryableForStatusCodeWhenHasConfigured() {
		EndpointResponseInternalServerErrorException internalServerErrorException = new EndpointResponseInternalServerErrorException("internal server error",
				Headers.empty(), "bla");
		assertTrue(matcher.match(internalServerErrorException));
	}

	@Test
	public void shouldBeRetryableForAny4xxStatusCodeWhenHasConfigured() {
		EndpointResponseBadRequestException badRequestException = new EndpointResponseBadRequestException("bad request",
				Headers.empty(), "bla");
		assertTrue(matcher.match(badRequestException));
	}

	@Test
	public void shouldBeRetryableForHeaderWhenHasConfigured() {
		EndpointResponseGatewayTimeoutException gatewayTimeoutException = new EndpointResponseGatewayTimeoutException("gateway timeout",
				new Headers(Header.of("X-Header-Retry", "retry!")), "bla");
		assertTrue(matcher.match(gatewayTimeoutException));
	}

	@Test
	public void shouldBeRetryableForEndpointResponseWhenHasConfigured() {
		EndpointResponse<String> response = new EndpointResponse<>(StatusCode.of(429), new Headers(Header.of("X-Header-Retry-2", "retry!")),
				"bla");
		EndpointResponseException exception = new EndpointResponseException("whatever error", response);

		assertTrue(matcher.match(exception));
	}

	@SuppressWarnings("serial")
	private class MyException extends Exception {
	}
}
