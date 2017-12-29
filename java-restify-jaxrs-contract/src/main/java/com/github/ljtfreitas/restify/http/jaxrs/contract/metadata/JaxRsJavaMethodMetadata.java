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
package com.github.ljtfreitas.restify.http.jaxrs.contract.metadata;

import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;

import com.github.ljtfreitas.restify.reflection.JavaAnnotationScanner;
import com.github.ljtfreitas.restify.reflection.JavaTypeResolver;

class JaxRsJavaMethodMetadata {

	private final java.lang.reflect.Method javaMethod;
	private final Path path;
	private final HttpMethod httpMethod;
	private final Consumes consumes;
	private final Produces produces;

	public JaxRsJavaMethodMetadata(java.lang.reflect.Method javaMethod) {
		this.javaMethod = javaMethod;

		JavaAnnotationScanner annotationScanner = new JavaAnnotationScanner(javaMethod);
		
		this.path = annotationScanner.scan(Path.class).orElse(null);

		this.httpMethod = annotationScanner.scan(HttpMethod.class)
				.orElseThrow(() -> new IllegalArgumentException("Method " + javaMethod + " does not have a @HttpMethod annotation"));

		this.consumes = javaMethod.getAnnotation(Consumes.class);
		this.produces = javaMethod.getAnnotation(Produces.class);
	}

	public Optional<Path> path() {
		return Optional.ofNullable(path);
	}

	public HttpMethod httpMethod() {
		return httpMethod;
	}

	public JaxRsEndpointHeader[] headers() {
		Collection<JaxRsEndpointHeader> headers = new LinkedHashSet<>();

		Optional.ofNullable(consumes)
			.ifPresent(c -> headers.add(contentType()));

		Optional.ofNullable(produces)
			.ifPresent(c -> headers.add(accept()));

		return headers.toArray(new JaxRsEndpointHeader[0]);
	}

	private JaxRsEndpointHeader contentType() {
		isTrue(consumes.value().length <= 1, "Only one value is allowed for the @Consumes annotation.");
		isTrue(consumes.value().length == 1, "Enter the @Consumes annotation value.");

		return new JaxRsEndpointHeader(HttpHeaders.CONTENT_TYPE, consumes.value()[0]);
	}

	private JaxRsEndpointHeader accept() {
		isTrue(produces.value().length > 0, "Enter the @Produces annotation value.");

		String accept = Arrays.stream(produces.value()).collect(Collectors.joining(", "));

		return new JaxRsEndpointHeader(HttpHeaders.ACCEPT, accept);
	}

	public Type returnType(Class<?> rawType) {
		return new JavaTypeResolver(rawType).returnTypeOf(javaMethod);
	}
}
