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

import static com.github.ljtfreitas.restify.util.Preconditions.isFalse;
import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;

class JaxRsJavaTypeMetadata {

	private final Class<?> javaType;
	private final ApplicationPath applicationPath;
	private final Path path;
	private final Consumes consumes;
	private final Produces produces;
	private final JaxRsJavaTypeMetadata parent;

	public JaxRsJavaTypeMetadata(Class<?> javaType) {
		isTrue(javaType.isInterface(), "Your type must be a Java interface.");
		isTrue(javaType.getInterfaces().length <= 1, "Only single inheritance is supported.");

		this.javaType = javaType;
		this.parent = javaType.getInterfaces().length == 1 ? new JaxRsJavaTypeMetadata(javaType.getInterfaces()[0]) : null;

		this.applicationPath = javaType.getAnnotation(ApplicationPath.class);
		this.path = javaType.getAnnotation(Path.class);

		isFalse(applicationPath != null && path != null,
				"Invalid use of the @Path and @ApplicationPath annotations at the top of the interface. Use only one.");

		this.consumes = javaType.getAnnotation(Consumes.class);
		this.produces = javaType.getAnnotation(Produces.class);
	}

	public String[] paths() {
		return Stream.concat(Arrays.stream(allPaths()), Arrays.stream(allApplicationPaths()))
				.toArray(s -> new String[s]);
	}

	private String[] allPaths() {
		ArrayList<String> paths = new ArrayList<>();

		Optional.ofNullable(parent)
			.map(p -> p.allPaths())
				.ifPresent(array -> Collections.addAll(paths, array));

		Optional.ofNullable(path).map(Path::value)
			.ifPresent(p -> paths.add(p));

		return paths.toArray(new String[0]);
	}

	private String[] allApplicationPaths() {
		ArrayList<String> applicationPaths = new ArrayList<>();

		Optional.ofNullable(parent)
			.map(p -> p.allApplicationPaths())
				.ifPresent(array -> Collections.addAll(applicationPaths, array));

		Optional.ofNullable(applicationPath).map(ApplicationPath::value)
			.ifPresent(p -> applicationPaths.add(p));

		return applicationPaths.toArray(new String[0]);
	}

	public JaxRsEndpointHeader[] headers() {
		Collection<JaxRsEndpointHeader> headers = new LinkedHashSet<>();

		Optional.ofNullable(parent)
			.ifPresent(p -> headers.addAll(Arrays.asList(parent.headers())));

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

	public Class<?> javaType() {
		return javaType;
	}

	public Optional<JaxRsJavaTypeMetadata> parent() {
		return Optional.ofNullable(parent);
	}
}
