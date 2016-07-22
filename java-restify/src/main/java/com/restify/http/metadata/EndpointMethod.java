package com.restify.http.metadata;

import java.lang.reflect.Method;
import java.util.Objects;

public class EndpointMethod {

	private final Method javaMethod;
	private final String path;
	private final String httpMethod;
	private final EndpointMethodParameters parameters;
	private final EndpointHeaders headers;

	public EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters,
			EndpointHeaders headers) {

		this.javaMethod = javaMethod;
		this.path = path;
		this.httpMethod = httpMethod;
		this.parameters = parameters;
		this.headers = headers;
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
