package com.restify.http.client.response;

import com.restify.http.client.Headers;

public class EndpointResponse<T> {

	private final EndpointResponseCode code;
	private final Headers headers;
	private final T body;

	public EndpointResponse(EndpointResponseCode code, Headers headers, T body) {
		this.code = code;
		this.headers = headers;
		this.body = body;
	}

	public Headers headers() {
		return headers;
	}

	public EndpointResponseCode code() {
		return code;
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
					.append(code)
				.append(", ")
				.append("Headers: ")
					.append(headers)
				.append(", ")
				.append("Body: ")
					.append(body)
			.append("]");

		return report.toString();
	}

	public static <T> EndpointResponse<T> empty(EndpointResponseCode code, Headers headers) {
		return new EndpointResponse<T>(code, headers, null);
	}
}
