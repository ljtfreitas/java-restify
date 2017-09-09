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

import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.Header;
import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextPlainMessageConverter;

public class EndpointResponseExceptionFactory {

	private static final TextPlainMessageConverter TEXT_ERROR_RESPONSE_MESSAGE_CONVERTER = new TextPlainMessageConverter();

	public RestifyEndpointResponseException create(HttpResponseMessage response) {
		String bodyAsString = TEXT_ERROR_RESPONSE_MESSAGE_CONVERTER.read(response, String.class);

		String responseBody = (bodyAsString != null && !bodyAsString.isEmpty()) ? bodyAsString : "(empty)";

		String message = new StringBuilder()
				.append("HTTP request: ")
					.append("[")
						.append(response.request().method())
							.append(" ")
							.append(response.request().uri())
					.append("]")
					.append("\n")
				.append("HTTP response: ")
					.append("[")
						.append(response.statusCode())
					.append("]")
						.append("\n")
					.append("Headers: ")
						.append(response.headers().all().stream()
							.map(Header::toString)
								.collect(Collectors.joining(", ", "[", "]")))
						.append("\n")
				.append(responseBody)
			.toString();

		StatusCode statusCode = response.statusCode();

		if (statusCode.isBadRequest()) {
			return onBadRequest(message, response.headers(), bodyAsString);

		} else if (statusCode.isUnauthorized()) {
			return onUnauthorized(message, response.headers(), bodyAsString);

		} else if (statusCode.isForbidden()) {
			return onForbidden(message, response.headers(), bodyAsString);

		} else if (statusCode.isNotFound()) {
			return onNotFound(message, response.headers(), bodyAsString);

		} else if (statusCode.isMethodNotAllowed()) {
			return onMethodNotAllowed(message, response.headers(), bodyAsString);

		} else if (statusCode.isNotAcceptable()) {
			return onNotAcceptable(message, response.headers(), bodyAsString);

		} else if (statusCode.isProxyAuthenticationRequired()) {
			return onProxyAuthenticationRequired(message, response.headers(), bodyAsString);

		} else if (statusCode.isRequestTimeout()) {
			return onRequestTimeout(message, response.headers(), bodyAsString);

		} else if (statusCode.isConflict()) {
			return onConflict(message, response.headers(), bodyAsString);

		} else if (statusCode.isGone()) {
			return onGone(message, response.headers(), bodyAsString);

		} else if (statusCode.isLengthRequired()) {
			return onLengthRequired(message, response.headers(), bodyAsString);

		} else if (statusCode.isPreconditionFailed()) {
			return onPreconditionFailed(message, response.headers(), bodyAsString);

		} else if (statusCode.isRequestEntityTooLarge()) {
			return onRequestEntityTooLarge(message, response.headers(), bodyAsString);

		} else if (statusCode.isRequestUriTooLong()) {
			return onRequestUriTooLong(message, response.headers(), bodyAsString);

		} else if (statusCode.isUnsupportedMediaType()) {
			return onUnsupportedMediaType(message, response.headers(), bodyAsString);

		} else if (statusCode.isRequestedRangeNotSatisfiable()) {
			return onRequestedRangeNotSatisfiable(message, response.headers(), bodyAsString);

		} else if (statusCode.isExpectationFailed()) {
			return onExpectationFailed(message, response.headers(), bodyAsString);

		} else if (statusCode.isInternalServerError()) {
			return onInternalServerError(message, response.headers(), bodyAsString);

		} else if (statusCode.isNotImplemented()) {
			return onNotImplemented(message, response.headers(), bodyAsString);

		} else if (statusCode.isBadGateway()) {
			return onBadGateway(message, response.headers(), bodyAsString);

		} else if (statusCode.isServiceUnavailable()) {
			return onServiceUnavailable(message, response.headers(), bodyAsString);

		} else if (statusCode.isGatewayTimeout()) {
			return onGatewayTimeout(message, response.headers(), bodyAsString);

		} else if (statusCode.isHttpVersionNotSupported()) {
			return onHttpVersionNotSupported(message, response.headers(), bodyAsString);

		} else {
			return unhandled(message, response.statusCode(), response.headers(), bodyAsString);
		}
	}

	private RestifyEndpointResponseBadRequestException onBadRequest(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseBadRequestException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseUnauthorizedException onUnauthorized(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseUnauthorizedException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseForbiddenException onForbidden(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseForbiddenException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseNotFoundException onNotFound(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseNotFoundException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseMethodNotAllowedException onMethodNotAllowed(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseMethodNotAllowedException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseNotAcceptableException onNotAcceptable(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseNotAcceptableException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseProxyAuthenticationRequiredException onProxyAuthenticationRequired(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseProxyAuthenticationRequiredException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseRequestTimeoutException onRequestTimeout(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseRequestTimeoutException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseConflictException onConflict(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseConflictException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseGoneException onGone(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseGoneException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseLengthRequiredException onLengthRequired(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseLengthRequiredException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponsePreconditionFailedException onPreconditionFailed(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponsePreconditionFailedException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseRequestEntityTooLargeException onRequestEntityTooLarge(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseRequestEntityTooLargeException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseRequestUriTooLongException onRequestUriTooLong(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseRequestUriTooLongException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseUnsupportedMediaTypeException onUnsupportedMediaType(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseUnsupportedMediaTypeException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseRequestedRangeNotSatisfiableException onRequestedRangeNotSatisfiable(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseRequestedRangeNotSatisfiableException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseExpectationFailedException onExpectationFailed(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseExpectationFailedException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseInternalServerErrorException onInternalServerError(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseInternalServerErrorException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseNotImplementedException onNotImplemented(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseNotImplementedException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseBadGatewayException onBadGateway(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseBadGatewayException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseServiceUnavailableException onServiceUnavailable(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseServiceUnavailableException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseGatewayTimeoutException onGatewayTimeout(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseGatewayTimeoutException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseHttpVersionNotSupportedException onHttpVersionNotSupported(String message, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseHttpVersionNotSupportedException(message, headers, bodyAsString);
	}

	private RestifyEndpointResponseException unhandled(String message, StatusCode statusCode, Headers headers, String bodyAsString) {
		return new RestifyEndpointResponseException(message, statusCode, headers, bodyAsString);
	}
}
