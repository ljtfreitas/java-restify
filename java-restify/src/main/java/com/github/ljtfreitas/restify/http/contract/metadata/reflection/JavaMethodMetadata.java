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
package com.github.ljtfreitas.restify.http.contract.metadata.reflection;

import java.lang.reflect.Type;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.contract.Header;
import com.github.ljtfreitas.restify.http.contract.Headers;
import com.github.ljtfreitas.restify.http.contract.Method;
import com.github.ljtfreitas.restify.http.contract.Path;
import com.github.ljtfreitas.restify.http.contract.Version;

public class JavaMethodMetadata {

	private final java.lang.reflect.Method javaMethod;
	private final Path path;
	private final Method httpMethod;
	private final Header[] headers;
	private final Version version;

	public JavaMethodMetadata(java.lang.reflect.Method javaMethod) {
		this.javaMethod = javaMethod;

		this.path = javaMethod.getAnnotation(Path.class);

		this.httpMethod = Optional.ofNullable(new JavaAnnotationScanner(javaMethod).scan(Method.class))
				.orElseThrow(() -> new IllegalArgumentException("Method " + javaMethod + " does not have a @Method annotation"));

		this.headers = Optional.ofNullable(javaMethod.getAnnotation(Headers.class))
				.map(Headers::value)
					.orElseGet(() -> new JavaAnnotationScanner(javaMethod).scanAll(Header.class));

		this.version = javaMethod.getAnnotation(Version.class);
	}

	public Optional<Path> path() {
		return Optional.ofNullable(path);
	}

	public Method httpMethod() {
		return httpMethod;
	}

	public java.lang.reflect.Method javaMethod() {
		return javaMethod;
	}

	public Header[] headers() {
		return headers;
	}

	public Type returnType(Class<?> rawType) {
		return new JavaTypeResolver(rawType).returnTypeOf(javaMethod);
	}

	public Optional<Version> version() {
		return Optional.ofNullable(version);
	}
}
