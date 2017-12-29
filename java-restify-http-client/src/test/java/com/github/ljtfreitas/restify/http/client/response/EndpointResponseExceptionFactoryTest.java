package com.github.ljtfreitas.restify.http.client.response;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;

@RunWith(MockitoJUnitRunner.class)
public class EndpointResponseExceptionFactoryTest {

	private EndpointResponseExceptionFactory exceptionFactory;

	@Mock(answer = Answers.RETURNS_MOCKS)
	private HttpResponseMessage response;
	
	@Before
	public void setup() {
		exceptionFactory = new EndpointResponseExceptionFactory();
		
		when(response.headers()).thenReturn(new Headers());
		when(response.body()).thenReturn(new ByteArrayInputStream(new byte[0]));
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsBadRequest() {
		when(response.status()).thenReturn(StatusCode.badRequest());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseBadRequestException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsUnauthorized() {
		when(response.status()).thenReturn(StatusCode.unauthorized());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseUnauthorizedException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsForbidden() {
		when(response.status()).thenReturn(StatusCode.forbidden());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseForbiddenException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsNotFound() {
		when(response.status()).thenReturn(StatusCode.notFound());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseNotFoundException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsMethodNotAllowed() {
		when(response.status()).thenReturn(StatusCode.methodNotAllowed());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseMethodNotAllowedException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsNotAcceptable() {
		when(response.status()).thenReturn(StatusCode.notAcceptable());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseNotAcceptableException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsProxyAuthenticationRequired() {
		when(response.status()).thenReturn(StatusCode.proxyAuthenticationRequired());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseProxyAuthenticationRequiredException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsRequestTimeout() {
		when(response.status()).thenReturn(StatusCode.requestTimeout());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseRequestTimeoutException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsConflict() {
		when(response.status()).thenReturn(StatusCode.conflict());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseConflictException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsGone() {
		when(response.status()).thenReturn(StatusCode.gone());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseGoneException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsLengthRequired() {
		when(response.status()).thenReturn(StatusCode.lengthRequired());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseLengthRequiredException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsRequestEntityTooLarge() {
		when(response.status()).thenReturn(StatusCode.requestEntityTooLarge());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseRequestEntityTooLargeException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsRequestUriTooLong() {
		when(response.status()).thenReturn(StatusCode.requestUriTooLong());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseRequestUriTooLongException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsUnsupportedMediaType() {
		when(response.status()).thenReturn(StatusCode.unsupportedMediaType());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseUnsupportedMediaTypeException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsRequestedRangeNotSatisfiable() {
		when(response.status()).thenReturn(StatusCode.requestedRangeNotSatisfiable());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseRequestedRangeNotSatisfiableException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsExpectationFailed() {
		when(response.status()).thenReturn(StatusCode.expectationFailed());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseExpectationFailedException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsInternalServerError() {
		when(response.status()).thenReturn(StatusCode.internalServerError());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseInternalServerErrorException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsNotImplemented() {
		when(response.status()).thenReturn(StatusCode.notImplemented());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseNotImplementedException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsBadGateway() {
		when(response.status()).thenReturn(StatusCode.badGateway());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseBadGatewayException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsServiceUnavailable() {
		when(response.status()).thenReturn(StatusCode.serviceUnavailable());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseServiceUnavailableException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsGatewayTimeout() {
		when(response.status()).thenReturn(StatusCode.gatewayTimeout());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseGatewayTimeoutException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsHttpVersionNotSupported() {
		when(response.status()).thenReturn(StatusCode.httpVersionNotSupported());

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseHttpVersionNotSupportedException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}

	@Test
	public void shouldThrowExceptionWhenStatusCodeIsUnhandled() {
		when(response.status()).thenReturn(StatusCode.of(999));

		EndpointResponseException exception = exceptionFactory.create(response);

		assertThat(exception, instanceOf(EndpointResponseException.class));
		assertThat(exception.status(), equalTo(response.status()));
		assertThat(exception.headers(), sameInstance(response.headers()));
		assertThat(exception.bodyAsString(), isEmptyString());
	}
}
