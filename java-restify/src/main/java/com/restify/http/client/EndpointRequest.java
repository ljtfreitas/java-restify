package com.restify.http.client;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.restify.http.RestifyHttpException;

public class EndpointRequest {

	private final URI endpoint;
	private final String method;
	private final Headers headers;
	private final Object body;
	private final Type expectedType;

	public EndpointRequest(URI endpoint, String method) {
		this(endpoint, method, new Headers(), null, void.class);
	}

	public EndpointRequest(URI endpoint, String method, Type expectedType) {
		this(endpoint, method, new Headers(), null, expectedType);
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, Object body, Type expectedType) {
		this.endpoint = endpoint;
		this.method = method;
		this.headers = headers;
		this.body = body;
		this.expectedType = expectedType;
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

	public Type expectedType() {
		return expectedType;
	}

	public EndpointRequest newParameter(String name, String value) {
		String appender = endpoint.getQuery() == null ? "" : "&";

		String newQuery = Optional.ofNullable(endpoint.getRawQuery())
				.orElse("")
					.concat(appender)
						.concat(name + "=" + value);

		try {
			URI newURI = new URI(endpoint.getScheme(), endpoint.getRawAuthority(), endpoint.getRawPath(),
					newQuery, endpoint.getRawFragment());

			return new EndpointRequest(newURI, method, headers, body, expectedType);
		} catch (URISyntaxException e) {
			throw new RestifyHttpException(e);
		}
	}
}
