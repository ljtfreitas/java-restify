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

import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.contract.Header;
import com.github.ljtfreitas.restify.http.contract.Headers;
import com.github.ljtfreitas.restify.http.contract.Path;
import com.github.ljtfreitas.restify.http.contract.Version;
import com.github.ljtfreitas.restify.reflection.JavaAnnotationScanner;

class ContractTypeMetadata {

	private final Class<?> javaType;
	private final Path path;
	private final Collection<Header> headers;
	private final Version version;
	private final ContractTypeMetadata parent;

	public ContractTypeMetadata(Class<?> javaType) {
		isTrue(javaType.isInterface(), "Your type must be a Java interface.");
		isTrue(javaType.getInterfaces().length <= 1, "Only single inheritance is supported.");

		this.javaType = javaType;
		this.parent = javaType.getInterfaces().length == 1 ? new ContractTypeMetadata(javaType.getInterfaces()[0]) : null;

		JavaAnnotationScanner annotationScanner = new JavaAnnotationScanner(javaType);
		
		this.path = annotationScanner.scan(Path.class).orElse(null);

		this.headers = Optional.ofNullable(javaType.getAnnotation(Headers.class))
				.map(Headers::value)
					.map(array -> Arrays.asList(array))
						.orElseGet(() -> new ArrayList<>(annotationScanner.scanAll(Header.class)));

		this.version = javaType.getAnnotation(Version.class);
	}

	public Optional<Path> path() {
		return Optional.ofNullable(path);
	}

	public Path[] paths() {
		ArrayList<Path> paths = new ArrayList<>();

		Optional.ofNullable(parent)
			.map(p -> p.paths())
				.ifPresent(array -> Collections.addAll(paths, array));

		Optional.ofNullable(path)
			.ifPresent(p -> paths.add(p));

		return paths.toArray(new Path[0]);
	}

	public Collection<Header> headers() {
		Collection<Header> headers = new ArrayList<>();

		Optional.ofNullable(parent)
			.map(p -> p.headers())
				.ifPresent(h -> headers.addAll(h));

		headers.addAll(this.headers);

		return headers;
	}

	public Class<?> javaType() {
		return javaType;
	}

	public Optional<ContractTypeMetadata> parent() {
		return Optional.ofNullable(parent);
	}

	public Optional<Version> version() {
		return version == null ? Optional.ofNullable(parent).flatMap(ContractTypeMetadata::version) : Optional.of(version);
	}
}
