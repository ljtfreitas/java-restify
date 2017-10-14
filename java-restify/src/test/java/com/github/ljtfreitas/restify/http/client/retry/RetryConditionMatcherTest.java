package com.github.ljtfreitas.restify.http.client.retry;

import static com.github.ljtfreitas.restify.http.client.retry.RetryCondition.StatusCodeRetryCondition.any4xx;
import static com.github.ljtfreitas.restify.http.client.retry.RetryCondition.ThrowableRetryCondition.ioFailure;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.SocketException;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.header.Headers;
import com.github.ljtfreitas.restify.http.client.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseBadRequestException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseGatewayTimeoutException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseInternalServerErrorException;

public class RetryConditionMatcherTest {

	private RetryConditionMatcher matcher;

	@Before
	public void setup() {
		RetryConfiguration configuration = new RetryConfiguration.Builder()
				.when(MyException.class)
				.when(HttpStatusCode.INTERNAL_SERVER_ERROR, HttpStatusCode.GATEWAY_TIMEOUT)
				.when(any4xx())
				.when(ioFailure())
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
	public void ioCauseShouldBeRetryableWhenIOFailureIsConfigured() {
		assertTrue(matcher.match(new RuntimeException(new SocketException())));
	}

	@Test
	public void shouldBeRetryableForInternalServerErrorWhenHasConfigured() {
		RestifyEndpointResponseInternalServerErrorException internalServerErrorException = new RestifyEndpointResponseInternalServerErrorException("internal server error",
				Headers.empty(), "bla");
		assertTrue(matcher.match(internalServerErrorException));
	}

	@Test
	public void shouldBeRetryableForGatewayTimeoutWhenHasConfigured() {
		RestifyEndpointResponseGatewayTimeoutException gatewayTimeoutException = new RestifyEndpointResponseGatewayTimeoutException("gateway timeout", Headers.empty(), "bla");
		assertTrue(matcher.match(gatewayTimeoutException));
	}

	@Test
	public void shouldBeRetryableForAny4xxStatusCodeWhenHasConfigured() {
		RestifyEndpointResponseBadRequestException badRequestException = new RestifyEndpointResponseBadRequestException("bad request", Headers.empty(), "bla");
		assertTrue(matcher.match(badRequestException));
	}

	@SuppressWarnings("serial")
	private class MyException extends Exception {
	}
}
