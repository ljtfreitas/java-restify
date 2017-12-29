package com.github.ljtfreitas.restify.http.client.call.async;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.async.EndpointResponseFailureCallback;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseBadGatewayException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseBadRequestException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseConflictException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseExpectationFailedException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseForbiddenException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseGatewayTimeoutException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseGoneException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseHttpVersionNotSupportedException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseInternalServerErrorException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseLengthRequiredException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseMethodNotAllowedException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseNotAcceptableException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseNotFoundException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseNotImplementedException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponsePreconditionFailedException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseProxyAuthenticationRequiredException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseRequestEntityTooLargeException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseRequestTimeoutException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseRequestUriTooLongException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseRequestedRangeNotSatisfiableException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseServiceUnavailableException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseUnauthorizedException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseUnsupportedMediaTypeException;

@RunWith(MockitoJUnitRunner.class)
public class EndpointResponseFailureCallbackTest {

	@Spy
	private EndpointResponseFailureCallback callback;

	@Test
	public void shouldCallBadRequestCallbackWhenBadRequestErrorOcurred() {
		EndpointResponseBadRequestException exception = new EndpointResponseBadRequestException(
				"http error", new Headers(), "bad request");
		callback.onFailure(exception);

		verify(callback).onBadRequest(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallUnauthorizedCallbackWhenUnauthorizedErrorOcurred() {
		EndpointResponseUnauthorizedException exception = new EndpointResponseUnauthorizedException(
				"http error", new Headers(), "unauthorized");
		callback.onFailure(exception);

		verify(callback).onUnauthorized(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallForbiddenCallbackWhenForbiddenErrorOcurred() {
		EndpointResponseForbiddenException exception = new EndpointResponseForbiddenException(
				"http error", new Headers(), "forbidden");
		callback.onFailure(exception);

		verify(callback).onForbidden(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallNotFoundCallbackWhenNotFoundErrorOcurred() {
		EndpointResponseNotFoundException exception = new EndpointResponseNotFoundException("http error",
				new Headers(), "not found");
		callback.onFailure(exception);

		verify(callback).onNotFound(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallMethodNotAllowedCallbackWhenMethodNotAllowedErrorOcurred() {
		EndpointResponseMethodNotAllowedException exception = new EndpointResponseMethodNotAllowedException(
				"http error", new Headers(), "method not allowed");
		callback.onFailure(exception);

		verify(callback).onMethodNotAllowed(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallNotAcceptableCallbackWhenNotAcceptableErrorOcurred() {
		EndpointResponseNotAcceptableException exception = new EndpointResponseNotAcceptableException(
				"http error", new Headers(), "not acceptable");
		callback.onFailure(exception);

		verify(callback).onNotAcceptable(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallProxyAuthenticationRequiredCallbackWhenProxyAuthenticationRequiredErrorOcurred() {
		EndpointResponseProxyAuthenticationRequiredException exception = new EndpointResponseProxyAuthenticationRequiredException(
				"http error", new Headers(), "proxy authentication required");
		callback.onFailure(exception);

		verify(callback).onProxyAuthenticationRequired(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallRequestTimeoutCallbackWhenRequestTimeoutErrorOcurred() {
		EndpointResponseRequestTimeoutException exception = new EndpointResponseRequestTimeoutException(
				"http error", new Headers(), "request timeout");
		callback.onFailure(exception);

		verify(callback).onRequestTimeout(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallConflictCallbackWhenConflictErrorOcurred() {
		EndpointResponseConflictException exception = new EndpointResponseConflictException("http error",
				new Headers(), "conflict");
		callback.onFailure(exception);

		verify(callback).onConflict(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallGoneCallbackWhenGoneErrorOcurred() {
		EndpointResponseGoneException exception = new EndpointResponseGoneException("http error",
				new Headers(), "gone");
		callback.onFailure(exception);

		verify(callback).onGone(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallLenghtRequiredCallbackWhenLenghtRequiredErrorOcurred() {
		EndpointResponseLengthRequiredException exception = new EndpointResponseLengthRequiredException(
				"http error", new Headers(), "length required");
		callback.onFailure(exception);

		verify(callback).onLengthRequired(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallPreconditionFailedCallbackWhenPreconditionFailedErrorOcurred() {
		EndpointResponsePreconditionFailedException exception = new EndpointResponsePreconditionFailedException(
				"http error", new Headers(), "precondition failed");
		callback.onFailure(exception);

		verify(callback).onPreconditionFailed(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallRequestEntityTooLargeCallbackWhenRequestEntityTooLargeErrorOcurred() {
		EndpointResponseRequestEntityTooLargeException exception = new EndpointResponseRequestEntityTooLargeException(
				"http error", new Headers(), "request entity too large");
		callback.onFailure(exception);

		verify(callback).onRequestEntityTooLarge(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallRequestUriTooLongCallbackWhenRequestUriTooLongErrorOcurred() {
		EndpointResponseRequestUriTooLongException exception = new EndpointResponseRequestUriTooLongException(
				"http error", new Headers(), "request uri too long");
		callback.onFailure(exception);

		verify(callback).onRequestUriTooLong(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallUnsupportedMediaTypeCallbackWhenUnsupportedMediaTypeErrorOcurred() {
		EndpointResponseUnsupportedMediaTypeException exception = new EndpointResponseUnsupportedMediaTypeException(
				"http error", new Headers(), "unsupported media type");
		callback.onFailure(exception);

		verify(callback).onUnsupportedMediaType(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallRequestedRangeNotSatisfiableCallbackWhenRequestedRangeNotSatisfiableErrorOcurred() {
		EndpointResponseRequestedRangeNotSatisfiableException exception = new EndpointResponseRequestedRangeNotSatisfiableException(
				"http error", new Headers(), "requested range not satisfiable");
		callback.onFailure(exception);

		verify(callback).onRequestedRangeNotSatisfiable(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallExpectationFailedCallbackWhenExpectationFailedErrorOcurred() {
		EndpointResponseExpectationFailedException exception = new EndpointResponseExpectationFailedException(
				"http error", new Headers(), "expectation failed");
		callback.onFailure(exception);

		verify(callback).onExpectationFailed(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallInternalServerErrorCallbackWhenInternalServerErrorErrorOcurred() {
		EndpointResponseInternalServerErrorException exception = new EndpointResponseInternalServerErrorException(
				"http error", new Headers(), "internal server error");
		callback.onFailure(exception);

		verify(callback).onInternalServerError(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallNotImplementedCallbackWhenNotImplementedErrorOcurred() {
		EndpointResponseNotImplementedException exception = new EndpointResponseNotImplementedException(
				"http error", new Headers(), "not implemented");
		callback.onFailure(exception);

		verify(callback).onNotImplemented(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallBadGatewayCallbackWhenBadGatewayErrorOcurred() {
		EndpointResponseBadGatewayException exception = new EndpointResponseBadGatewayException(
				"http error", new Headers(), "bad gateway");
		callback.onFailure(exception);

		verify(callback).onBadGateway(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallServiceUnavailableCallbackWhenServiceUnavailableOcurred() {
		EndpointResponseServiceUnavailableException exception = new EndpointResponseServiceUnavailableException(
				"http error", new Headers(), "service unavailable");
		callback.onFailure(exception);

		verify(callback).onServiceUnavailable(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallGatewayTimeoutCallbackWhenGatewayTimeoutOcurred() {
		EndpointResponseGatewayTimeoutException exception = new EndpointResponseGatewayTimeoutException(
				"http error", new Headers(), "gateway timeout");
		callback.onFailure(exception);

		verify(callback).onGatewayTimeout(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallHttpVersionNotSupportedCallbackWhenHttpVersionNotSupportedOcurred() {
		EndpointResponseHttpVersionNotSupportedException exception = new EndpointResponseHttpVersionNotSupportedException(
				"http error", new Headers(), "http version not supported");
		callback.onFailure(exception);

		verify(callback).onHttpVersionNotSupported(argThat(responseMatchWith(exception)));
	}

	private Matcher<EndpointResponse<String>> responseMatchWith(EndpointResponseException exception) {
		return new ArgumentMatcher<EndpointResponse<String>>() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean matches(Object argument) {
				EndpointResponse<String> response = (EndpointResponse<String>) argument;
				return exception.status() == response.status()
						&& exception.headers() == response.headers()
						&& Optional.ofNullable(exception.bodyAsString()).orElse("").equals(response.body());
			}
		};
	}
}
