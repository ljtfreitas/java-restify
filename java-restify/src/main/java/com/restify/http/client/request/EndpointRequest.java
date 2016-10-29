package com.restify.http.client.request;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.Headers;
import com.restify.http.contract.metadata.reflection.JavaType;

public class EndpointRequest {

	private final URI endpoint;
	private final String method;
	private final Headers headers;
	private final Object body;
	private final JavaType responseType;

	public EndpointRequest(URI endpoint, String method) {
		this(endpoint, method, new Headers(), null, void.class);
	}

	public EndpointRequest(URI endpoint, String method, Type responseType) {
		this(endpoint, method, new Headers(), responseType);
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, Type responseType) {
		this(endpoint, method, headers, null, responseType);
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, Object body, Type responseType) {
		this(endpoint, method, headers, body, JavaType.of(responseType));
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, Object body, JavaType responseType) {
		this.endpoint = endpoint;
		this.method = method;
		this.headers = headers;
		this.body = body;
		this.responseType = responseType;
	}

	public URI endpoint() {
		return endpoint;
	}

	public String method() {
		return method;
	}

	public Optional<Object> body() {
		return Optional.ofNullable(body);
	}

	public Headers headers() {
		return headers;
	}

	public JavaType responseType() {
		return responseType;
	}

	public EndpointRequest appendParameter(String name, String value) {
		String appender = endpoint.getQuery() == null ? "" : "&";

		String newQuery = Optional.ofNullable(endpoint.getRawQuery())
				.orElse("")
					.concat(appender)
						.concat(name + "=" + value);

		try {
			URI newURI = new URI(endpoint.getScheme(), endpoint.getRawAuthority(), endpoint.getRawPath(),
					newQuery, endpoint.getRawFragment());

			return new EndpointRequest(newURI, method, headers, body, responseType);
		} catch (URISyntaxException e) {
			throw new RestifyHttpException(e);
		}
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("EndpointRequest: [")
				.append("URI: ")
					.append(endpoint)
				.append(", ")
				.append("HTTP Method: ")
					.append(method)
				.append(", ")
				.append("Body: ")
					.append(body)
			.append("]");

		return report.toString();
	}
}
