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
package com.github.ljtfreitas.restify.http.client.message.response;

import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.ACCEPTED;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.BAD_GATEWAY;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.BAD_REQUEST;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.CONFLICT;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.CONTINUE;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.CREATED;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.EXPECTIATION_FAILED;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.FORBIDDEN;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.FOUND;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.GATEWAY_TIMEOUT;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.GONE;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.HTTP_VERSION_NOT_SUPPORTED;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.INTERNAL_SERVER_ERROR;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.LENGHT_REQUIRED;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.METHOD_NOT_ALLOWED;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.NON_AUTHORITATIVE_INFORMATION;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.NOT_ACCEPTABLE;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.NOT_FOUND;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.NOT_IMPLEMENTED;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.NOT_MODIFIED;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.NO_CONTENT;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.OK;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.PARTIAL_CONTENT;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.PRECONDITION_FAILED;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.PROXY_AUTHENTATION_REQUIRED;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.REQUESTED_RANGE_NOT_SATISFIABLE;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.REQUEST_ENTITY_TOO_LARGE;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.REQUEST_TIMEOUT;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.REQUEST_URI_TOO_LONG;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.RESET_CONTENT;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.SERVICE_UNAVAILABLE;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.SWITCHING_PROTOCOLS;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.UNAUTHORIZED;
import static com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode.UNSUPPORTED_MEDIA_TYPE;

import java.util.Objects;
import java.util.Optional;

public class StatusCode {

	private final int code;
	private final String message;

	private StatusCode(int code) {
		this(code, "");
	}

	private StatusCode(HttpStatusCode httpStatusCode) {
		this(httpStatusCode.value(), httpStatusCode.message());
	}

	private StatusCode(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int value() {
		return code;
	}

	public int message() {
		return code;
	}

	public boolean is(int code) {
		return Integer.compare(this.code, code) == 0;
	}

	public boolean is(HttpStatusCode httpStatusCode) {
		return Integer.compare(this.code, httpStatusCode.value()) == 0;
	}

	public boolean isInformational() {
		return (code / 100) == 1;
	}

	public boolean isSucessful() {
		return (code / 100) == 2;
	}

	public boolean isRedirection() {
		return (code / 100) == 3;
	}

	public boolean isClientError() {
		return (code / 100) == 4;
	}

	public boolean isServerError() {
		return (code / 100) == 5;
	}

	public boolean isError() {
		return isClientError() || isServerError();
	}

	public boolean isContinue() {
		return code == CONTINUE.value();
	}

	public boolean isSwitchingProtocols() {
		return code == SWITCHING_PROTOCOLS.value();
	}

	public boolean isOk() {
		return code == OK.value();
	}

	public boolean isCreated() {
		return code == CREATED.value();
	}

	public boolean isAccepted() {
		return code == ACCEPTED.value();
	}

	public boolean isNonAuthoritativeInformation() {
		return code == NON_AUTHORITATIVE_INFORMATION.value();
	}

	public boolean isNoContent() {
		return code == NO_CONTENT.value();
	}

	public boolean isResetContent() {
		return code == RESET_CONTENT.value();
	}

	public boolean isPartialContent() {
		return code == PARTIAL_CONTENT.value();
	}

	public boolean isNotModified() {
		return code == NOT_MODIFIED.value();
	}

	public boolean isBadRequest() {
		return code == BAD_REQUEST.value();
	}

	public boolean isUnauthorized() {
		return code == UNAUTHORIZED.value();
	}

	public boolean isForbidden() {
		return code == FORBIDDEN.value();
	}

	public boolean isNotFound() {
		return code == NOT_FOUND.value();
	}

	public boolean isMethodNotAllowed() {
		return code == METHOD_NOT_ALLOWED.value();
	}

	public boolean isNotAcceptable() {
		return code == NOT_ACCEPTABLE.value();
	}

	public boolean isProxyAuthenticationRequired() {
		return code == PROXY_AUTHENTATION_REQUIRED.value();
	}

	public boolean isRequestTimeout() {
		return code == REQUEST_TIMEOUT.value();
	}

	public boolean isConflict() {
		return code == CONFLICT.value();
	}

	public boolean isGone() {
		return code == GONE.value();
	}

	public boolean isLengthRequired() {
		return code == LENGHT_REQUIRED.value();
	}

	public boolean isPreconditionFailed() {
		return code == PRECONDITION_FAILED.value();
	}

	public boolean isRequestEntityTooLarge() {
		return code == REQUEST_ENTITY_TOO_LARGE.value();
	}

	public boolean isRequestUriTooLong() {
		return code == REQUEST_URI_TOO_LONG.value();
	}

	public boolean isUnsupportedMediaType() {
		return code == UNSUPPORTED_MEDIA_TYPE.value();
	}

	public boolean isRequestedRangeNotSatisfiable() {
		return code == REQUESTED_RANGE_NOT_SATISFIABLE.value();
	}

	public boolean isExpectationFailed() {
		return code == EXPECTIATION_FAILED.value();
	}

	public boolean isInternalServerError() {
		return code == INTERNAL_SERVER_ERROR.value();
	}

	public boolean isNotImplemented() {
		return code == NOT_IMPLEMENTED.value();
	}

	public boolean isBadGateway() {
		return code == BAD_GATEWAY.value();
	}

	public boolean isServiceUnavailable() {
		return code == SERVICE_UNAVAILABLE.value();
	}

	public boolean isGatewayTimeout() {
		return code == GATEWAY_TIMEOUT.value();
	}

	public boolean isHttpVersionNotSupported() {
		return code == HTTP_VERSION_NOT_SUPPORTED.value();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StatusCode) {
			StatusCode that = (StatusCode) obj;
			return Integer.valueOf(code).equals(that.code);

		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(code);
	}

	@Override
	public String toString() {
		return Integer.toString(code) + " " + message;
	}

	public static StatusCode of(int code) {
		Optional<HttpStatusCode> httpStatusCode = HttpStatusCode.of(code);
		return httpStatusCode.isPresent() ? new StatusCode(httpStatusCode.get()) : new StatusCode(code);
	}

	public static StatusCode of(int code, String message) {
		Optional<HttpStatusCode> httpStatusCode = HttpStatusCode.of(code);
		return httpStatusCode.isPresent() ? new StatusCode(httpStatusCode.get()) : new StatusCode(code, message);
	}

	public static StatusCode of(HttpStatusCode httpStatusCode) {
		return new StatusCode(httpStatusCode);
	}

	public static StatusCode ok() {
		return new StatusCode(OK);
	}

	public static StatusCode created() {
		return new StatusCode(CREATED);
	}
	
	public static StatusCode noContent() {
		return new StatusCode(NO_CONTENT);
	}

	public static StatusCode found() {
		return new StatusCode(FOUND);
	}

	public static StatusCode notModified() {
		return new StatusCode(NOT_MODIFIED);
	}

	public static StatusCode badRequest() {
		return new StatusCode(BAD_REQUEST);
	}

	public static StatusCode unauthorized() {
		return new StatusCode(UNAUTHORIZED);
	}

	public static StatusCode forbidden() {
		return new StatusCode(FORBIDDEN);
	}

	public static StatusCode notFound() {
		return new StatusCode(NOT_FOUND);
	}

	public static StatusCode methodNotAllowed() {
		return new StatusCode(METHOD_NOT_ALLOWED);
	}

	public static StatusCode notAcceptable() {
		return new StatusCode(NOT_ACCEPTABLE);
	}

	public static StatusCode proxyAuthenticationRequired() {
		return new StatusCode(PROXY_AUTHENTATION_REQUIRED);
	}

	public static StatusCode conflict() {
		return new StatusCode(CONFLICT);
	}

	public static StatusCode gone() {
		return new StatusCode(GONE);
	}

	public static StatusCode lengthRequired() {
		return new StatusCode(LENGHT_REQUIRED);
	}

	public static StatusCode preconditionFailed() {
		return new StatusCode(PRECONDITION_FAILED);
	}

	public static StatusCode requestEntityTooLarge() {
		return new StatusCode(REQUEST_ENTITY_TOO_LARGE);
	}

	public static StatusCode requestUriTooLong() {
		return new StatusCode(REQUEST_URI_TOO_LONG);
	}

	public static StatusCode unsupportedMediaType() {
		return new StatusCode(UNSUPPORTED_MEDIA_TYPE);
	}

	public static StatusCode requestedRangeNotSatisfiable() {
		return new StatusCode(REQUESTED_RANGE_NOT_SATISFIABLE);
	}

	public static StatusCode expectationFailed() {
		return new StatusCode(EXPECTIATION_FAILED);
	}

	public static StatusCode internalServerError() {
		return new StatusCode(INTERNAL_SERVER_ERROR);
	}

	public static StatusCode notImplemented() {
		return new StatusCode(NOT_IMPLEMENTED);
	}

	public static StatusCode badGateway() {
		return new StatusCode(BAD_GATEWAY);
	}

	public static StatusCode serviceUnavailable() {
		return new StatusCode(SERVICE_UNAVAILABLE);
	}

	public static StatusCode gatewayTimeout() {
		return new StatusCode(GATEWAY_TIMEOUT);
	}

	public static StatusCode httpVersionNotSupported() {
		return new StatusCode(HTTP_VERSION_NOT_SUPPORTED);
	}

	public static StatusCode requestTimeout() {
		return new StatusCode(REQUEST_TIMEOUT);
	}

}
