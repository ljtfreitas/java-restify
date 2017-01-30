package com.github.ljtfreitas.restify.http.client.response;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.Headers;

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

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseBadRequestException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsUnauthorized() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.unauthorized(), headers, responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseUnauthorizedException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsForbidden() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.forbidden(), headers, responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseForbiddenException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsNotFound() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.notFound(), headers, responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseNotFoundException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsMethodNotAllowed() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.methodNotAllowed(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseMethodNotAllowedException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsNotAcceptable() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.notAcceptable(), headers, responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseNotAcceptableException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsProxyAuthenticationRequired() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.proxyAuthenticationRequired(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseProxyAuthenticationRequiredException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsRequestTimeout() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.requestTimeout(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseRequestTimeoutException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsConflict() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.conflict(), headers, responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseConflictException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsGone() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.gone(), headers, responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseGoneException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsLengthRequired() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.lengthRequired(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseLengthRequiredException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsRequestEntityTooLarge() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.requestEntityTooLarge(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseRequestEntityTooLargeException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsRequestUriTooLong() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.requestUriTooLong(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseRequestUriTooLongException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsUnsupportedMediaType() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.unsupportedMediaType(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseUnsupportedMediaTypeException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsRequestedRangeNotSatisfiable() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.requestedRangeNotSatisfiable(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseRequestedRangeNotSatisfiableException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsExpectationFailed() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.expectationFailed(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseExpectationFailedException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsInternalServerError() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.internalServerError(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseInternalServerErrorException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsNotImplemented() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.notImplemented(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseNotImplementedException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsBadGateway() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.badGateway(), headers, responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseBadGatewayException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsServiceUnavailable() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.serviceUnavailable(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseServiceUnavailableException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsGatewayTimeout() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.gatewayTimeout(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseGatewayTimeoutException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsHttpVersionNotSupported() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.httpVersionNotSupported(), headers,
				responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseHttpVersionNotSupportedException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsUnhandled() {
		HttpResponseMessage response = new SimpleHttpResponseMessage(StatusCode.of(999), headers, responseBody);

		RestifyEndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(RestifyEndpointResponseException.class));
		assertThat(exception.statusCode(), equalTo(response.statusCode()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}
}
