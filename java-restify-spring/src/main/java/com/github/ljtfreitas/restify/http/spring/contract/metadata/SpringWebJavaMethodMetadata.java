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
package com.github.ljtfreitas.restify.http.spring.contract.metadata;

import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.ljtfreitas.restify.reflection.JavaTypeResolver;

class SpringWebJavaMethodMetadata {

	private final Method javaMethod;
	private final SpringWebRequestMappingMetadata mapping;

	public SpringWebJavaMethodMetadata(Method javaMethod) {
		this.javaMethod = javaMethod;

		RequestMapping mapping = Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(javaMethod, RequestMapping.class))
				.orElseThrow(() -> new IllegalArgumentException("Method [" + javaMethod + "] does not have a @RequestMapping annotation."));

		isTrue(mapping.value().length <= 1, "Only single path is allowed.");
		isTrue(mapping.method().length == 1, "You must set the HTTP method (only one!) of your Java method [" + javaMethod + "].");

		this.mapping = new SpringWebRequestMappingMetadata(mapping);
	}

	public Optional<String> path() {
		return mapping.path();
	}

	public RequestMethod httpMethod() {
		return mapping.method()[0];
	}

	public Type returnType(Class<?> rawType) {
		return new JavaTypeResolver(rawType).returnTypeOf(javaMethod);
	}

	public String[] headers() {
		return mapping.headers();
	}
}
