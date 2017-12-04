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

import static com.github.ljtfreitas.restify.util.Preconditions.isFalse;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.ljtfreitas.restify.http.contract.ParameterSerializer;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.JavaTypeResolver;

class SpringWebJavaMethodParameterMetadata {

	private final JavaType type;
	private final String name;
	private final PathVariable pathParameter;
	private final RequestHeader headerParameter;
	private final RequestBody bodyParameter;
	private final RequestParam queryParameter;
	private final Class<? extends ParameterSerializer> serializerType;

	public SpringWebJavaMethodParameterMetadata(java.lang.reflect.Parameter javaMethodParameter) {
		this(javaMethodParameter, javaMethodParameter.getDeclaringExecutable().getDeclaringClass());
	}

	public SpringWebJavaMethodParameterMetadata(java.lang.reflect.Parameter javaMethodParameter, Class<?> targetClassType) {
		this.type = JavaType.of(new JavaTypeResolver(targetClassType).parameterizedTypeOf(javaMethodParameter));

		this.pathParameter = AnnotationUtils.synthesizeAnnotation(javaMethodParameter.getAnnotation(PathVariable.class),
				javaMethodParameter);

		this.headerParameter = AnnotationUtils.synthesizeAnnotation(javaMethodParameter.getAnnotation(RequestHeader.class),
				javaMethodParameter);

		this.bodyParameter = AnnotationUtils.synthesizeAnnotation(javaMethodParameter.getAnnotation(RequestBody.class),
				javaMethodParameter);

		this.queryParameter = AnnotationUtils.synthesizeAnnotation(javaMethodParameter.getAnnotation(RequestParam.class),
				javaMethodParameter);

		isFalse(Stream.of(pathParameter, headerParameter, bodyParameter, queryParameter).allMatch(a -> a == null),
				"The parameter [" + javaMethodParameter.getName() + "] of method [" + javaMethodParameter.getDeclaringExecutable() + "] is not annotated with "
						+ "@PathVariable, @RequestHeader, @RequestBody or @RequestParam");

		String name = Optional.ofNullable(pathParameter)
				.map(PathVariable::value).filter(s -> !s.trim().isEmpty())
					.orElseGet(() -> Optional.ofNullable(headerParameter)
						.map(RequestHeader::value).filter(s -> !s.trim().isEmpty())
							.orElseGet(() -> Optional.ofNullable(queryParameter)
								.map(RequestParam::value).filter(s -> !s.trim().isEmpty())
									.orElseGet(() -> Optional.ofNullable(javaMethodParameter.getName())
											.filter(n -> javaMethodParameter.isNamePresent() && !n.isEmpty())
												.orElse(null))));

		isFalse(needName() && name == null, "Could not get the name of the parameter [" + javaMethodParameter + "], "
				+ "from method [" + javaMethodParameter.getDeclaringExecutable() + "]");
		this.name = name;

		this.serializerType = SpringWebEndpointMethodParameterSerializer.of(this);
	}

	private boolean needName() {
		return pathParameter != null || queryParameter != null || headerParameter != null;
	}

	public String name() {
		return name;
	}

	public JavaType javaType() {
		return type;
	}

	public boolean path() {
		return pathParameter != null;
	}

	public boolean body() {
		return bodyParameter != null;
	}

	public boolean header() {
		return headerParameter != null;
	}

	public boolean query() {
		return queryParameter != null;
	}

	public Class<? extends ParameterSerializer> serializer() {
		return serializerType;
	}
}
