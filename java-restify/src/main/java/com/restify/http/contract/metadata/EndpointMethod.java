package com.restify.http.contract.metadata;

import static com.restify.http.util.Preconditions.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

import com.restify.http.contract.metadata.reflection.JavaType;

public class EndpointMethod {

	private final Method javaMethod;
	private final String path;
	private final String httpMethod;
	private final EndpointMethodParameters parameters;
	private final EndpointHeaders headers;
	private final JavaType returnType;

	public EndpointMethod(Method javaMethod, String path, String httpMethod) {
		this(javaMethod, path, httpMethod, new EndpointMethodParameters());
	}

	public EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters) {
		this(javaMethod, path, httpMethod, parameters, new EndpointHeaders());
	}

	public EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters,
			EndpointHeaders headers) {
		this(javaMethod, path, httpMethod, parameters, headers, (Type) null);
	}

	public EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters,
			EndpointHeaders headers, Type returnType) {
		this(javaMethod, path, httpMethod, parameters, headers, JavaType.of(Optional.ofNullable(returnType).orElseGet(() -> javaMethod.getGenericReturnType())));
	}

	private EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters,
			EndpointHeaders headers, JavaType returnType) {
		this.javaMethod = nonNull(javaMethod, "EndpointMethod needs a Java method.");
		this.path = nonNull(path, "EndpointMethod needs a endpoint path.");
		this.httpMethod = nonNull(httpMethod, "EndpointMethod needs a HTTP method.");
		this.parameters = nonNull(parameters, "EndpointMethod needs a parameters collection.");
		this.headers = nonNull(headers, "EndpointMethod needs a HTTP headers collection.");
		this.returnType = returnType;
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

	public JavaType returnType() {
		return returnType;
	}

	public boolean runnableAsync() {
		return returnType.voidType() && !parameters.callbacks().isEmpty();
	}

	public String expand(final Object[] args) {
		String endpoint = new EndpointPathParameterResolver(path, parameters)
				.resolve(args);

		String query = new EndpointQueryParameterResolver(parameters.ofQuery())
				.resolve(args);

		return endpoint + query;
	}

	public EndpointMethod with(JavaType returnType) {
		return new EndpointMethod(javaMethod, path, httpMethod, parameters, headers, returnType);
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
				.append(", ")
				.append("Method Return Type: ")
					.append(returnType)
			.append("]");

		return report.toString();
	}
}
