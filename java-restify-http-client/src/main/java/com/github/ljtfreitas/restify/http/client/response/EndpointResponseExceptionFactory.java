/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client.response;

import java.net.URI;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.message.ContentType;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReader;
import com.github.ljtfreitas.restify.http.client.message.converter.text.StringMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;

public class EndpointResponseExceptionFactory {

	private static final HttpMessageReader<String> TEXT_ERROR_RESPONSE_MESSAGE_CONVERTER = new SimpleStringMessageConverter();

	public EndpointResponseException create(HttpResponseMessage response) {
		String bodyAsString = TEXT_ERROR_RESPONSE_MESSAGE_CONVERTER.read(response, String.class);

		String responseBody = (bodyAsString != null && !bodyAsString.isEmpty()) ? bodyAsString : "(empty)";

		String message = new StringBuilder()
				.append("HTTP request: ")
					.append("[")
						.append(Optional.ofNullable(response.request()).map(HttpRequestMessage::method).orElse(""))
							.append(" ")
							.append(Optional.ofNullable(response.request()).map(HttpRequestMessage::uri).map(URI::toString).orElse(""))
					.append("]")
					.append("\n")
				.append("HTTP response: ")
					.append("[")
						.append(response.status())
					.append("]")
						.append("\n")
					.append("Headers: ")
						.append("[")
							.append(response.headers())
						.append("]")
					.append("\n")
				.append(responseBody)
			.toString();

		StatusCode statusCode = response.status();
		Headers headers = response.headers();

		if (statusCode.isBadRequest()) {
			return onBadRequest(message, headers, bodyAsString);

		} else if (statusCode.isUnauthorized()) {
			return onUnauthorized(message, headers, bodyAsString);

		} else if (statusCode.isForbidden()) {
			return onForbidden(message, headers, bodyAsString);

		} else if (statusCode.isNotFound()) {
			return onNotFound(message, headers, bodyAsString);

		} else if (statusCode.isMethodNotAllowed()) {
			return onMethodNotAllowed(message, headers, bodyAsString);

		} else if (statusCode.isNotAcceptable()) {
			return onNotAcceptable(message, headers, bodyAsString);

		} else if (statusCode.isProxyAuthenticationRequired()) {
			return onProxyAuthenticationRequired(message, headers, bodyAsString);

		} else if (statusCode.isRequestTimeout()) {
			return onRequestTimeout(message, headers, bodyAsString);

		} else if (statusCode.isConflict()) {
			return onConflict(message, headers, bodyAsString);

		} else if (statusCode.isGone()) {
			return onGone(message, headers, bodyAsString);

		} else if (statusCode.isLengthRequired()) {
			return onLengthRequired(message, headers, bodyAsString);

		} else if (statusCode.isPreconditionFailed()) {
			return onPreconditionFailed(message, headers, bodyAsString);

		} else if (statusCode.isRequestEntityTooLarge()) {
			return onRequestEntityTooLarge(message, headers, bodyAsString);

		} else if (statusCode.isRequestUriTooLong()) {
			return onRequestUriTooLong(message, headers, bodyAsString);

		} else if (statusCode.isUnsupportedMediaType()) {
			return onUnsupportedMediaType(message, headers, bodyAsString);

		} else if (statusCode.isRequestedRangeNotSatisfiable()) {
			return onRequestedRangeNotSatisfiable(message, headers, bodyAsString);

		} else if (statusCode.isExpectationFailed()) {
			return onExpectationFailed(message, headers, bodyAsString);

		} else if (statusCode.isInternalServerError()) {
			return onInternalServerError(message, headers, bodyAsString);

		} else if (statusCode.isNotImplemented()) {
			return onNotImplemented(message, headers, bodyAsString);

		} else if (statusCode.isBadGateway()) {
			return onBadGateway(message, headers, bodyAsString);

		} else if (statusCode.isServiceUnavailable()) {
			return onServiceUnavailable(message, headers, bodyAsString);

		} else if (statusCode.isGatewayTimeout()) {
			return onGatewayTimeout(message, headers, bodyAsString);

		} else if (statusCode.isHttpVersionNotSupported()) {
			return onHttpVersionNotSupported(message, headers, bodyAsString);

		} else {
			return unhandled(message, response.status(), headers, bodyAsString);
		}
	}

	private EndpointResponseBadRequestException onBadRequest(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseBadRequestException(message, headers, bodyAsString);
	}

	private EndpointResponseUnauthorizedException onUnauthorized(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseUnauthorizedException(message, headers, bodyAsString);
	}

	private EndpointResponseForbiddenException onForbidden(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseForbiddenException(message, headers, bodyAsString);
	}

	private EndpointResponseNotFoundException onNotFound(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseNotFoundException(message, headers, bodyAsString);
	}

	private EndpointResponseMethodNotAllowedException onMethodNotAllowed(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseMethodNotAllowedException(message, headers, bodyAsString);
	}

	private EndpointResponseNotAcceptableException onNotAcceptable(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseNotAcceptableException(message, headers, bodyAsString);
	}

	private EndpointResponseProxyAuthenticationRequiredException onProxyAuthenticationRequired(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseProxyAuthenticationRequiredException(message, headers, bodyAsString);
	}

	private EndpointResponseRequestTimeoutException onRequestTimeout(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseRequestTimeoutException(message, headers, bodyAsString);
	}

	private EndpointResponseConflictException onConflict(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseConflictException(message, headers, bodyAsString);
	}

	private EndpointResponseGoneException onGone(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseGoneException(message, headers, bodyAsString);
	}

	private EndpointResponseLengthRequiredException onLengthRequired(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseLengthRequiredException(message, headers, bodyAsString);
	}

	private EndpointResponsePreconditionFailedException onPreconditionFailed(String message, Headers headers, String bodyAsString) {
		return new EndpointResponsePreconditionFailedException(message, headers, bodyAsString);
	}

	private EndpointResponseRequestEntityTooLargeException onRequestEntityTooLarge(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseRequestEntityTooLargeException(message, headers, bodyAsString);
	}

	private EndpointResponseRequestUriTooLongException onRequestUriTooLong(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseRequestUriTooLongException(message, headers, bodyAsString);
	}

	private EndpointResponseUnsupportedMediaTypeException onUnsupportedMediaType(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseUnsupportedMediaTypeException(message, headers, bodyAsString);
	}

	private EndpointResponseRequestedRangeNotSatisfiableException onRequestedRangeNotSatisfiable(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseRequestedRangeNotSatisfiableException(message, headers, bodyAsString);
	}

	private EndpointResponseExpectationFailedException onExpectationFailed(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseExpectationFailedException(message, headers, bodyAsString);
	}

	private EndpointResponseInternalServerErrorException onInternalServerError(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseInternalServerErrorException(message, headers, bodyAsString);
	}

	private EndpointResponseNotImplementedException onNotImplemented(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseNotImplementedException(message, headers, bodyAsString);
	}

	private EndpointResponseBadGatewayException onBadGateway(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseBadGatewayException(message, headers, bodyAsString);
	}

	private EndpointResponseServiceUnavailableException onServiceUnavailable(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseServiceUnavailableException(message, headers, bodyAsString);
	}

	private EndpointResponseGatewayTimeoutException onGatewayTimeout(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseGatewayTimeoutException(message, headers, bodyAsString);
	}

	private EndpointResponseHttpVersionNotSupportedException onHttpVersionNotSupported(String message, Headers headers, String bodyAsString) {
		return new EndpointResponseHttpVersionNotSupportedException(message, headers, bodyAsString);
	}

	private EndpointResponseException unhandled(String message, StatusCode statusCode, Headers headers, String bodyAsString) {
		return new EndpointResponseException(message, statusCode, headers, bodyAsString);
	}
	
	private static class SimpleStringMessageConverter extends StringMessageConverter {

		@Override
		public ContentType contentType() {
			throw new UnsupportedOperationException();
		}
		
	}
}
