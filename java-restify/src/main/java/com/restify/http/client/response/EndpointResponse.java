package com.restify.http.client.response;

import com.restify.http.client.Headers;

public class EndpointResponse<T> {

	private final StatusCode statusCode;
	private final Headers headers;
	private final T body;

	public EndpointResponse(StatusCode statusCode, Headers headers, T body) {
		this.statusCode = statusCode;
		this.headers = headers;
		this.body = body;
	}

	public Headers headers() {
		return headers;
	}

	public StatusCode code() {
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
}
