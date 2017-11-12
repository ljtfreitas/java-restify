package com.github.ljtfreitas.restify.http.client.response;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;

public class EndpointResponseExceptionFactoryTest {

	private Headers headers;
	private ByteArrayInputStream responseBody;

	private EndpointResponseExceptionFactory exceptionFactory;

	@Before
	public void setup() {
		headers = new Headers();
		responseBody = new ByteArrayInputStream(new byte[0]);

		exceptionFactory = new EndpointResponseExceptionFactory();
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsBadRequest() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.badRequest(), headers, responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseBadRequestException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsUnauthorized() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.unauthorized(), headers, responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseUnauthorizedException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsForbidden() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.forbidden(), headers, responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseForbiddenException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsNotFound() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.notFound(), headers, responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseNotFoundException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsMethodNotAllowed() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.methodNotAllowed(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseMethodNotAllowedException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsNotAcceptable() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.notAcceptable(), headers, responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseNotAcceptableException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsProxyAuthenticationRequired() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.proxyAuthenticationRequired(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseProxyAuthenticationRequiredException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsRequestTimeout() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.requestTimeout(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseRequestTimeoutException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsConflict() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.conflict(), headers, responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseConflictException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsGone() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.gone(), headers, responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseGoneException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsLengthRequired() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.lengthRequired(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseLengthRequiredException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsRequestEntityTooLarge() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.requestEntityTooLarge(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseRequestEntityTooLargeException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsRequestUriTooLong() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.requestUriTooLong(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseRequestUriTooLongException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsUnsupportedMediaType() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.unsupportedMediaType(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseUnsupportedMediaTypeException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsRequestedRangeNotSatisfiable() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.requestedRangeNotSatisfiable(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseRequestedRangeNotSatisfiableException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsExpectationFailed() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.expectationFailed(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseExpectationFailedException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsInternalServerError() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.internalServerError(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseInternalServerErrorException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsNotImplemented() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.notImplemented(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseNotImplementedException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsBadGateway() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.badGateway(), headers, responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseBadGatewayException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsServiceUnavailable() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.serviceUnavailable(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseServiceUnavailableException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsGatewayTimeout() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.gatewayTimeout(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseGatewayTimeoutException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsHttpVersionNotSupported() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.httpVersionNotSupported(), headers,
				responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseHttpVersionNotSupportedException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsUnhandled() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.of(999), headers, responseBody);

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}
}
