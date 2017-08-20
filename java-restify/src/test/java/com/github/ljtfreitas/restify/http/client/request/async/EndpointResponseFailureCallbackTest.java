package com.github.ljtfreitas.restify.http.client.request.async;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.header.Headers;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseBadGatewayException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseBadRequestException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseConflictException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseExpectationFailedException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseForbiddenException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseGatewayTimeoutException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseGoneException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseHttpVersionNotSupportedException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseInternalServerErrorException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseLengthRequiredException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseMethodNotAllowedException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseNotAcceptableException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseNotFoundException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseNotImplementedException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponsePreconditionFailedException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseProxyAuthenticationRequiredException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseRequestEntityTooLargeException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseRequestTimeoutException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseRequestUriTooLongException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseRequestedRangeNotSatisfiableException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseServiceUnavailableException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseUnauthorizedException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseUnsupportedMediaTypeException;

@RunWith(MockitoJUnitRunner.class)
public class EndpointResponseFailureCallbackTest {

	@Spy
	private EndpointResponseFailureCallback callback;

	@Test
	public void shouldCallBadRequestCallbackWhenBadRequestErrorOcurred() {
		RestifyEndpointResponseBadRequestException exception = new RestifyEndpointResponseBadRequestException(
				"http error", new Headers(), "bad request");
		callback.onFailure(exception);

		verify(callback).onBadRequest(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallUnauthorizedCallbackWhenUnauthorizedErrorOcurred() {
		RestifyEndpointResponseUnauthorizedException exception = new RestifyEndpointResponseUnauthorizedException(
				"http error", new Headers(), "unauthorized");
		callback.onFailure(exception);

		verify(callback).onUnauthorized(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallForbiddenCallbackWhenForbiddenErrorOcurred() {
		RestifyEndpointResponseForbiddenException exception = new RestifyEndpointResponseForbiddenException(
				"http error", new Headers(), "forbidden");
		callback.onFailure(exception);

		verify(callback).onForbidden(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallNotFoundCallbackWhenNotFoundErrorOcurred() {
		RestifyEndpointResponseNotFoundException exception = new RestifyEndpointResponseNotFoundException("http error",
				new Headers(), "not found");
		callback.onFailure(exception);

		verify(callback).onNotFound(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallMethodNotAllowedCallbackWhenMethodNotAllowedErrorOcurred() {
		RestifyEndpointResponseMethodNotAllowedException exception = new RestifyEndpointResponseMethodNotAllowedException(
				"http error", new Headers(), "method not allowed");
		callback.onFailure(exception);

		verify(callback).onMethodNotAllowed(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallNotAcceptableCallbackWhenNotAcceptableErrorOcurred() {
		RestifyEndpointResponseNotAcceptableException exception = new RestifyEndpointResponseNotAcceptableException(
				"http error", new Headers(), "not acceptable");
		callback.onFailure(exception);

		verify(callback).onNotAcceptable(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallProxyAuthenticationRequiredCallbackWhenProxyAuthenticationRequiredErrorOcurred() {
		RestifyEndpointResponseProxyAuthenticationRequiredException exception = new RestifyEndpointResponseProxyAuthenticationRequiredException(
				"http error", new Headers(), "proxy authentication required");
		callback.onFailure(exception);

		verify(callback).onProxyAuthenticationRequired(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallRequestTimeoutCallbackWhenRequestTimeoutErrorOcurred() {
		RestifyEndpointResponseRequestTimeoutException exception = new RestifyEndpointResponseRequestTimeoutException(
				"http error", new Headers(), "request timeout");
		callback.onFailure(exception);

		verify(callback).onRequestTimeout(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallConflictCallbackWhenConflictErrorOcurred() {
		RestifyEndpointResponseConflictException exception = new RestifyEndpointResponseConflictException("http error",
				new Headers(), "conflict");
		callback.onFailure(exception);

		verify(callback).onConflict(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallGoneCallbackWhenGoneErrorOcurred() {
		RestifyEndpointResponseGoneException exception = new RestifyEndpointResponseGoneException("http error",
				new Headers(), "gone");
		callback.onFailure(exception);

		verify(callback).onGone(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallLenghtRequiredCallbackWhenLenghtRequiredErrorOcurred() {
		RestifyEndpointResponseLengthRequiredException exception = new RestifyEndpointResponseLengthRequiredException(
				"http error", new Headers(), "length required");
		callback.onFailure(exception);

		verify(callback).onLengthRequired(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallPreconditionFailedCallbackWhenPreconditionFailedErrorOcurred() {
		RestifyEndpointResponsePreconditionFailedException exception = new RestifyEndpointResponsePreconditionFailedException(
				"http error", new Headers(), "precondition failed");
		callback.onFailure(exception);

		verify(callback).onPreconditionFailed(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallRequestEntityTooLargeCallbackWhenRequestEntityTooLargeErrorOcurred() {
		RestifyEndpointResponseRequestEntityTooLargeException exception = new RestifyEndpointResponseRequestEntityTooLargeException(
				"http error", new Headers(), "request entity too large");
		callback.onFailure(exception);

		verify(callback).onRequestEntityTooLarge(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallRequestUriTooLongCallbackWhenRequestUriTooLongErrorOcurred() {
		RestifyEndpointResponseRequestUriTooLongException exception = new RestifyEndpointResponseRequestUriTooLongException(
				"http error", new Headers(), "request uri too long");
		callback.onFailure(exception);

		verify(callback).onRequestUriTooLong(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallUnsupportedMediaTypeCallbackWhenUnsupportedMediaTypeErrorOcurred() {
		RestifyEndpointResponseUnsupportedMediaTypeException exception = new RestifyEndpointResponseUnsupportedMediaTypeException(
				"http error", new Headers(), "unsupported media type");
		callback.onFailure(exception);

		verify(callback).onUnsupportedMediaType(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallRequestedRangeNotSatisfiableCallbackWhenRequestedRangeNotSatisfiableErrorOcurred() {
		RestifyEndpointResponseRequestedRangeNotSatisfiableException exception = new RestifyEndpointResponseRequestedRangeNotSatisfiableException(
				"http error", new Headers(), "requested range not satisfiable");
		callback.onFailure(exception);

		verify(callback).onRequestedRangeNotSatisfiable(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallExpectationFailedCallbackWhenExpectationFailedErrorOcurred() {
		RestifyEndpointResponseExpectationFailedException exception = new RestifyEndpointResponseExpectationFailedException(
				"http error", new Headers(), "expectation failed");
		callback.onFailure(exception);

		verify(callback).onExpectationFailed(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallInternalServerErrorCallbackWhenInternalServerErrorErrorOcurred() {
		RestifyEndpointResponseInternalServerErrorException exception = new RestifyEndpointResponseInternalServerErrorException(
				"http error", new Headers(), "internal server error");
		callback.onFailure(exception);

		verify(callback).onInternalServerError(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallNotImplementedCallbackWhenNotImplementedErrorOcurred() {
		RestifyEndpointResponseNotImplementedException exception = new RestifyEndpointResponseNotImplementedException(
				"http error", new Headers(), "not implemented");
		callback.onFailure(exception);

		verify(callback).onNotImplemented(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallBadGatewayCallbackWhenBadGatewayErrorOcurred() {
		RestifyEndpointResponseBadGatewayException exception = new RestifyEndpointResponseBadGatewayException(
				"http error", new Headers(), "bad gateway");
		callback.onFailure(exception);

		verify(callback).onBadGateway(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallServiceUnavailableCallbackWhenServiceUnavailableOcurred() {
		RestifyEndpointResponseServiceUnavailableException exception = new RestifyEndpointResponseServiceUnavailableException(
				"http error", new Headers(), "service unavailable");
		callback.onFailure(exception);

		verify(callback).onServiceUnavailable(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallGatewayTimeoutCallbackWhenGatewayTimeoutOcurred() {
		RestifyEndpointResponseGatewayTimeoutException exception = new RestifyEndpointResponseGatewayTimeoutException(
				"http error", new Headers(), "gateway timeout");
		callback.onFailure(exception);

		verify(callback).onGatewayTimeout(argThat(responseMatchWith(exception)));
	}

	@Test
	public void shouldCallHttpVersionNotSupportedCallbackWhenHttpVersionNotSupportedOcurred() {
		RestifyEndpointResponseHttpVersionNotSupportedException exception = new RestifyEndpointResponseHttpVersionNotSupportedException(
				"http error", new Headers(), "http version not supported");
		callback.onFailure(exception);

		verify(callback).onHttpVersionNotSupported(argThat(responseMatchWith(exception)));
	}

	private Matcher<EndpointResponse<String>> responseMatchWith(RestifyEndpointResponseException exception) {
		return new ArgumentMatcher<EndpointResponse<String>>() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean matches(Object argument) {
				EndpointResponse<String> response = (EndpointResponse<String>) argument;
				return exception.statusCode() == response.code()
						&& exception.headers() == response.headers()
						&& Optional.ofNullable(exception.bodyAsString()).orElse("").equals(response.body());
			}
		};
	}
}
