package com.restify.http.metadata;

import static com.restify.http.metadata.Preconditions.nonNull;

import java.lang.reflect.Method;
import java.util.Objects;

public class EndpointMethod {

	private final Method javaMethod;
	private final String path;
	private final String httpMethod;
	private final EndpointMethodParameters parameters;
	private final EndpointHeaders headers;

	public EndpointMethod(Method javaMethod, String path, String httpMethod) {
		this(javaMethod, path, httpMethod, new EndpointMethodParameters());
	}

	public EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters) {
		this(javaMethod, path, httpMethod, parameters, new EndpointHeaders());
	}

	public EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters,
			EndpointHeaders headers) {

		this.javaMethod = nonNull(javaMethod, "EndpointMethod needs a Java method.");
		this.path = nonNull(path, "EndpointMethod needs a endpoint path.");
		this.httpMethod = nonNull(httpMethod, "EndpointMethod needs a HTTP method.");
		this.parameters = nonNull(parameters, "EndpointMethod needs a parameters collection.");
		this.headers = nonNull(headers, "EndpointMethod needs a HTTP headers collection.");
	}

	public String path() {
		return path;
	}

	public String httpMethod() {
		return httpMethod;
	}

	public Method javaMethod() {
		return javaMethod;
	}

	public EndpointMethodParameters parameters() {
		return parameters;
	}

	public EndpointHeaders headers() {
		return headers;
	}

	public Class<?> expectedType() {
		return javaMethod.getReturnType();
	}

	public String expand(final Object[] args) {
		return new EndpointPathParameterResolver(path, parameters)
				.resolve(args);
	}

	@Override
	public int hashCode() {
		return Objects.hash(javaMethod);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EndpointMethod) {
			EndpointMethod that = (EndpointMethod) obj;
			return javaMethod.equals(that.javaMethod);

		} else return false;
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("EndpointMethod: [")
				.append("Java Method: ")
					.append(javaMethod)
				.append(", ")
				.append("Endpoint Path: ")
					.append(path)
				.append(", ")
				.append("HTTP Method: ")
					.append(httpMethod)
			.append("]");

		return report.toString();
	}
}
