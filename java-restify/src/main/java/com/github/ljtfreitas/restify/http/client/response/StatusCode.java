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

public class StatusCode {

	// Informational 1xx
	public static final int HTTP_STATUS_CODE_CONTINUE = 100;

	// Successful 2xx
	public static final int HTTP_STATUS_CODE_OK = 200;
	public static final int HTTP_STATUS_CODE_NO_CONTENT = 204;

	// Redirection 3xx
	public static final int HTTP_STATUS_CODE_NOT_MODIFIED = 304;

	// Client Error 4xx
	public static final int HTTP_STATUS_CODE_BAD_REQUEST = 400;
	public static final int HTTP_STATUS_CODE_UNAUTHORIZED = 401;
	public static final int HTTP_STATUS_CODE_FORBIDDEN = 403;
	public static final int HTTP_STATUS_CODE_NOT_FOUND = 404;
	public static final int HTTP_STATUS_CODE_METHOD_NOT_ALLOWED = 405;
	public static final int HTTP_STATUS_CODE_NOT_ACCEPTABLE = 406;
	public static final int HTTP_STATUS_CODE_PROXY_AUTHENTATION_REQUIRED = 407;
	public static final int HTTP_STATUS_CODE_REQUEST_TIMEOUT = 408;
	public static final int HTTP_STATUS_CODE_CONFLICT = 409;
	public static final int HTTP_STATUS_CODE_GONE = 410;
	public static final int HTTP_STATUS_CODE_LENGHT_REQUIRED = 411;
	public static final int HTTP_STATUS_CODE_PRECONDITION_FAILED = 412;
	public static final int HTTP_STATUS_CODE_REQUEST_ENTITY_TOO_LARGE = 413;
	public static final int HTTP_STATUS_CODE_REQUEST_URI_TOO_LONG = 414;
	public static final int HTTP_STATUS_CODE_UNSUPPORTED_MEDIA_TYPE = 415;
	public static final int HTTP_STATUS_CODE_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
	public static final int HTTP_STATUS_CODE_EXPECTIATION_FAILED = 417;

	// Server Error 5xx
	public static final int HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR = 500;
	public static final int HTTP_STATUS_CODE_NOT_IMPLEMENTED = 501;
	public static final int HTTP_STATUS_CODE_BAD_GATEWAY = 502;
	public static final int HTTP_STATUS_CODE_SERVICE_UNAVAILABLE = 503;
	public static final int HTTP_STATUS_CODE_GATEWAY_TIMEOUT = 504;
	public static final int HTTP_STATUS_CODE_HTTP_VERSION_NOT_SUPPORTED = 505;

	private final int code;

	private StatusCode(int code) {
		this.code = code;
	}

	public int value() {
		return code;
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

	public boolean isNoContent() {
		return code == HTTP_STATUS_CODE_NO_CONTENT;
	}

	public boolean isNotModified() {
		return code == HTTP_STATUS_CODE_NOT_MODIFIED;
	}

	public boolean isBadRequest() {
		return code == HTTP_STATUS_CODE_BAD_REQUEST;
	}

	public boolean isUnauthorized() {
		return code == HTTP_STATUS_CODE_UNAUTHORIZED;
	}

	public boolean isForbidden() {
		return code == HTTP_STATUS_CODE_FORBIDDEN;
	}

	public boolean isNotFound() {
		return code == HTTP_STATUS_CODE_NOT_FOUND;
	}

	public boolean isMethodNotAllowed() {
		return code == HTTP_STATUS_CODE_METHOD_NOT_ALLOWED;
	}

	public boolean isNotAcceptable() {
		return code == HTTP_STATUS_CODE_NOT_ACCEPTABLE;
	}

	public boolean isProxyAuthenticationRequired() {
		return code == HTTP_STATUS_CODE_PROXY_AUTHENTATION_REQUIRED;
	}

	public boolean isRequestTimeout() {
		return code == HTTP_STATUS_CODE_REQUEST_TIMEOUT;
	}

	public boolean isConflict() {
		return code == HTTP_STATUS_CODE_CONFLICT;
	}

	public boolean isGone() {
		return code == HTTP_STATUS_CODE_GONE;
	}

	public boolean isLengthRequired() {
		return code == HTTP_STATUS_CODE_LENGHT_REQUIRED;
	}

	public boolean isPreconditionFailed() {
		return code == HTTP_STATUS_CODE_PRECONDITION_FAILED;
	}

	public boolean isRequestEntityTooLarge() {
		return code == HTTP_STATUS_CODE_REQUEST_ENTITY_TOO_LARGE;
	}

	public boolean isRequestUriTooLong() {
		return code == HTTP_STATUS_CODE_REQUEST_URI_TOO_LONG;
	}

	public boolean isUnsupportedMediaType() {
		return code == HTTP_STATUS_CODE_UNSUPPORTED_MEDIA_TYPE;
	}

	public boolean isRequestedRangeNotSatisfiable() {
		return code == HTTP_STATUS_CODE_REQUESTED_RANGE_NOT_SATISFIABLE;
	}

	public boolean isExpectationFailed() {
		return code == HTTP_STATUS_CODE_EXPECTIATION_FAILED;
	}

	public boolean isInternalServerError() {
		return code == HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR;
	}

	public boolean isNotImplemented() {
		return code == HTTP_STATUS_CODE_NOT_IMPLEMENTED;
	}

	public boolean isBadGateway() {
		return code == HTTP_STATUS_CODE_BAD_GATEWAY;
	}

	public boolean isServiceUnavailable() {
		return code == HTTP_STATUS_CODE_SERVICE_UNAVAILABLE;
	}

	public boolean isGatewayTimeout() {
		return code == HTTP_STATUS_CODE_GATEWAY_TIMEOUT;
	}

	public boolean isHttpVersionNotSupported() {
		return code == HTTP_STATUS_CODE_HTTP_VERSION_NOT_SUPPORTED;
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
	public String toString() {
		return Integer.toString(code);
	}

	public static StatusCode of(int code) {
		return new StatusCode(code);
	}

	public static StatusCode ok() {
		return new StatusCode(HTTP_STATUS_CODE_OK);
	}

	public static StatusCode noContent() {
		return new StatusCode(HTTP_STATUS_CODE_NO_CONTENT);
	}

	public static StatusCode notModified() {
		return new StatusCode(HTTP_STATUS_CODE_NOT_MODIFIED);
	}

	public static StatusCode badRequest() {
		return new StatusCode(HTTP_STATUS_CODE_BAD_REQUEST);
	}

	public static StatusCode unauthorized() {
		return new StatusCode(HTTP_STATUS_CODE_UNAUTHORIZED);
	}

	public static StatusCode forbidden() {
		return new StatusCode(HTTP_STATUS_CODE_FORBIDDEN);
	}

	public static StatusCode notFound() {
		return new StatusCode(HTTP_STATUS_CODE_NOT_FOUND);
	}

	public static StatusCode methodNotAllowed() {
		return new StatusCode(HTTP_STATUS_CODE_METHOD_NOT_ALLOWED);
	}

	public static StatusCode notAcceptable() {
		return new StatusCode(HTTP_STATUS_CODE_NOT_ACCEPTABLE);
	}

	public static StatusCode proxyAuthenticationRequired() {
		return new StatusCode(HTTP_STATUS_CODE_PROXY_AUTHENTATION_REQUIRED);
	}

	public static StatusCode conflict() {
		return new StatusCode(HTTP_STATUS_CODE_CONFLICT);
	}

	public static StatusCode gone() {
		return new StatusCode(HTTP_STATUS_CODE_GONE);
	}

	public static StatusCode lengthRequired() {
		return new StatusCode(HTTP_STATUS_CODE_LENGHT_REQUIRED);
	}

	public static StatusCode preconditionFailed() {
		return new StatusCode(HTTP_STATUS_CODE_PRECONDITION_FAILED);
	}

	public static StatusCode requestEntityTooLarge() {
		return new StatusCode(HTTP_STATUS_CODE_REQUEST_ENTITY_TOO_LARGE);
	}

	public static StatusCode requestUriTooLong() {
		return new StatusCode(HTTP_STATUS_CODE_REQUEST_URI_TOO_LONG);
	}

	public static StatusCode unsupportedMediaType() {
		return new StatusCode(HTTP_STATUS_CODE_UNSUPPORTED_MEDIA_TYPE);
	}

	public static StatusCode requestedRangeNotSatisfiable() {
		return new StatusCode(HTTP_STATUS_CODE_REQUESTED_RANGE_NOT_SATISFIABLE);
	}

	public static StatusCode expectationFailed() {
		return new StatusCode(HTTP_STATUS_CODE_EXPECTIATION_FAILED);
	}

	public static StatusCode internalServerError() {
		return new StatusCode(HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR);
	}

	public static StatusCode notImplemented() {
		return new StatusCode(HTTP_STATUS_CODE_NOT_IMPLEMENTED);
	}

	public static StatusCode badGateway() {
		return new StatusCode(HTTP_STATUS_CODE_BAD_GATEWAY);
	}

	public static StatusCode serviceUnavailable() {
		return new StatusCode(HTTP_STATUS_CODE_SERVICE_UNAVAILABLE);
	}

	public static StatusCode gatewayTimeout() {
		return new StatusCode(HTTP_STATUS_CODE_GATEWAY_TIMEOUT);
	}

	public static StatusCode httpVersionNotSupported() {
		return new StatusCode(HTTP_STATUS_CODE_HTTP_VERSION_NOT_SUPPORTED);
	}

	public static StatusCode requestTimeout() {
		return new StatusCode(HTTP_STATUS_CODE_REQUEST_TIMEOUT);
	}

}
