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

import java.util.Arrays;
import java.util.Optional;

public enum HttpStatusCode {

	// Informational 1xx
	CONTINUE(100, "Continue"),
	SWITCHING_PROTOCOLS(101, "Switching Protocols"),

	// Successful 2xx
	OK(200, "Ok"),
	CREATED(201, "Created"),
	ACCEPTED(202, "Accepted"),
	NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
	NO_CONTENT(204, "No Content"),
	RESET_CONTENT(205, "Reset Content"),
	PARTIAL_CONTENT(206, "Partial Content"),

	// Redirection 3xx
	MULTIPLES_CHOICES(300, "Multiple Choices"),
	MOVED_PERMANENTLY(301, "Moved Permanently"),
	FOUND(302, "Found"),
	SEE_OTHER(303, "See Other"),
	NOT_MODIFIED(304, "Not Modified"),
	USE_PROXY(305, "Use Proxy"),
	TEMPORARY_REDIRECT(307, "Temporary Redirect"),

	// Client Error 4xx
	BAD_REQUEST(400, "Bad Request"),
	UNAUTHORIZED(401, "Unauthorized"),
	FORBIDDEN(403, "Forbidden"),
	NOT_FOUND(404, "Not Found"),
	METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
	NOT_ACCEPTABLE(406, "Not Acceptable"),
	PROXY_AUTHENTATION_REQUIRED(407, "Proxy Authentication Required"),
	REQUEST_TIMEOUT(408, "Request Timeout"),
	CONFLICT(409, "Conflict"),
	GONE(410, "Gone"),
	LENGHT_REQUIRED(411, "Length Required"),
	PRECONDITION_FAILED(412, "Precondition Failed"),
	REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
	REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
	UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
	REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
	EXPECTIATION_FAILED(417, "Expectation Failed"),

	// Server Error 5xx
	INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
	NOT_IMPLEMENTED(501, "Not Implemented"),
	BAD_GATEWAY(502, "Bad Gateway"),
	SERVICE_UNAVAILABLE(503, "Service Unavailable"),
	GATEWAY_TIMEOUT(504, "Gateway Timeout"),
	HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported");

	private final int value;
	private final String message;

	private HttpStatusCode(int code, String message) {
		this.value = code;
		this.message = message;
	}

	public int value() {
		return value;
	}

	public String message() {
		return message;
	}

	public static Optional<HttpStatusCode> of(int code) {
		return Arrays.stream(HttpStatusCode.values()).filter(s -> s.value == code).findFirst();
	}
}
