package com.restify.http.client;

public class Response<T> {

	private final EndpointResponseCode code;
	private final Headers headers;
	private final T body;

	public Response(EndpointResponseCode code, Headers headers, T body) {
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
			.append("Response: [")
				.append("HTTP status code: ")
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

	public static <T> Response<T> of(EndpointResponseCode code, Headers headers, T body) {
		return new Response<>(code, headers, body);
	}
}
