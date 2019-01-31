package com.github.ljtfreitas.restify.http.client.response;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;

public class EndpointResponseTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private EndpointResponseInternalServerErrorException exception;

	private EndpointResponse<String> fallback;

	@Before
	public void setup() {
		exception = new EndpointResponseInternalServerErrorException("Internal server error", Headers.empty(), "body");

		fallback = EndpointResponse.of(StatusCode.ok(), "fallback");
	}

	@Test
	public void shouldReturnSuccessResponse() {
		EndpointResponse<String> response = EndpointResponse.of(StatusCode.ok(), "body");

		assertThat(response.status(), equalTo(StatusCode.ok()));
		assertThat(response.body(), equalTo("body"));
	}

	@Test
	public void shouldThrowExceptionOnErrorResponse() {
		EndpointResponse<String> response = EndpointResponse.error(exception);

		assertThat(response.status(), equalTo(exception.status()));

		expectedException.expect(exception.getClass());
		response.body();
	}

	@Test
	public void shouldRecoverFromErrorResponseUsingExceptionType() {
		EndpointResponse<String> response = EndpointResponse.<String> error(exception)
				.recover(EndpointResponseInternalServerErrorException.class, e -> fallback);

		assertThat(response.status(), equalTo(fallback.status()));
		assertThat(response.body(), equalTo(fallback.body()));
	}

	@Test
	public void shouldNotRecoverFromErrorResponseWhenExceptionTypeDoesNotMatch() {
		EndpointResponse<String> response = EndpointResponse.<String> error(exception)
				.recover(EndpointResponseBadRequestException.class, e -> fallback);

		assertThat(response.status(), equalTo(exception.status()));

		expectedException.expect(exception.getClass());
		response.body();
	}

	@Test
	public void shouldRecoverFromErrorResponseUsingExceptionPredicate() {
		EndpointResponse<String> response = EndpointResponse.<String> error(exception)
				.recover(e -> e.getClass().equals(exception.getClass()), e -> fallback);

		assertThat(response.status(), equalTo(fallback.status()));
		assertThat(response.body(), equalTo(fallback.body()));
	}

	@Test
	public void shouldNotRecoverFromErrorResponseWhenExceptionPredicateDoesNotMatch() {
		EndpointResponse<String> response = EndpointResponse.<String> error(exception)
				.recover(e -> e.getClass().equals(EndpointResponseBadRequestException.class), e -> fallback);

		assertThat(response.status(), equalTo(exception.status()));

		expectedException.expect(exception.getClass());
		response.body();
	}

	@Test
	public void shouldRecoverFromErrorResponseUsingExceptionTranslate() {
		EndpointResponse<String> response = EndpointResponse.<String> error(exception)
				.recover(e -> fallback);

		assertThat(response.status(), equalTo(fallback.status()));
		assertThat(response.body(), equalTo(fallback.body()));
	}
}
