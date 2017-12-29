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
package com.github.ljtfreitas.restify.http.contract.metadata;

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

import com.github.ljtfreitas.restify.reflection.JavaType;

public class EndpointMethod {

	private final Method javaMethod;
	private final String path;
	private final String httpMethod;
	private final EndpointMethodParameters parameters;
	private final EndpointHeaders headers;
	private final JavaType returnType;
	private final EndpointMethodMetadata metadata;
	private final String version;

	public EndpointMethod(Method javaMethod, String path, String httpMethod) {
		this(javaMethod, path, httpMethod, (String) null);
	}

	public EndpointMethod(Method javaMethod, String path, String httpMethod, String version) {
		this(javaMethod, path, httpMethod, new EndpointMethodParameters(), version);
	}

	public EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters) {
		this(javaMethod, path, httpMethod, parameters, new EndpointHeaders(), (String) null);
	}

	public EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters, String version) {
		this(javaMethod, path, httpMethod, parameters, new EndpointHeaders(), version);
	}

	public EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters,
			EndpointHeaders headers) {
		this(javaMethod, path, httpMethod, parameters, headers, (Type) null, (String) null);
	}

	public EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters,
			EndpointHeaders headers, String version) {
		this(javaMethod, path, httpMethod, parameters, headers, (Type) null, version);
	}

	public EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters,
			EndpointHeaders headers, Type returnType) {
		this(javaMethod, path, httpMethod, parameters, headers, returnType, (String) null);
	}

	public EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters,
			EndpointHeaders headers, Type returnType, String version) {
		this(javaMethod, path, httpMethod, parameters, headers, JavaType.of(Optional.ofNullable(returnType).orElseGet(() -> javaMethod.getGenericReturnType())),
				version);
	}

	private EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters,
			EndpointHeaders headers, JavaType returnType) {
		this(javaMethod, path, httpMethod, parameters, headers, returnType, null);
	}

	private EndpointMethod(Method javaMethod, String path, String httpMethod, EndpointMethodParameters parameters,
			EndpointHeaders headers, JavaType returnType, String version) {
		this.javaMethod = nonNull(javaMethod, "EndpointMethod needs a Java method.");
		this.path = nonNull(path, "EndpointMethod needs a endpoint path.");
		this.httpMethod = nonNull(httpMethod, "EndpointMethod needs a HTTP method.");
		this.parameters = nonNull(parameters, "EndpointMethod needs a parameters collection.");
		this.headers = nonNull(headers, "EndpointMethod needs a HTTP headers collection.");
		this.returnType = returnType;
		this.metadata = new EndpointMethodMetadata(javaMethod);
		this.version = version;
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

	public EndpointMethodMetadata metadata() {
		return metadata;
	}

	public Optional<String> version() {
		return Optional.ofNullable(version);
	}

	public String expand(final Object[] args) {
		String endpoint = new PathParameterResolver(path, parameters)
				.resolve(args);

		String query = new QueryParameterResolver(parameters.ofQuery())
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
