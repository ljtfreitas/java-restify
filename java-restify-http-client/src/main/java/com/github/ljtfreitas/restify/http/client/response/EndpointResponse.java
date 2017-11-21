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

import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;

public class EndpointResponse<T> {

	private final StatusCode statusCode;
	private final Headers headers;
	private final T body;

	public EndpointResponse(StatusCode statusCode, T body) {
		this(statusCode, new Headers(), body);
	}

	public EndpointResponse(StatusCode statusCode, Headers headers, T body) {
		this.statusCode = statusCode;
		this.headers = headers;
		this.body = body;
	}

	public Headers headers() {
		return headers;
	}

	public StatusCode status() {
		return statusCode;
	}

	public T body() {
		return body;
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("EndpointResponse: [")
				.append("HTTP Status code: ")
					.append(statusCode)
				.append(", ")
				.append("Headers: ")
					.append(headers)
				.append(", ")
				.append("Body: ")
					.append(body)
			.append("]");

		return report.toString();
	}

	public static <T> EndpointResponse<T> empty(StatusCode statusCode, Headers headers) {
		return new EndpointResponse<T>(statusCode, headers, null);
	}

	public static <T> EndpointResponse<T> error(StatusCode statusCode, Headers headers, String body) {
		isTrue(statusCode.isError(), "StatusCode [" + statusCode + "] is not a HTTP error!");

		return new EndpointResponse<T>(statusCode, headers, null) {
			@Override
			public T body() {
				String message = new StringBuilder("HTTP response is a error of type ")
						.append("[")
							.append(statusCode)
						.append("].")
							.append("\n")
						.append("Raw response body is:")
							.append("\n")
						.append(body)
							.toString();

				throw new EndpointResponseException(message, statusCode, headers, body);
			}
		};
	}
}
