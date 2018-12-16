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
import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.CookieValue;
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
	private final Annotation annotationParameter;
	private final Class<? extends ParameterSerializer> serializerType;

	public SpringWebJavaMethodParameterMetadata(java.lang.reflect.Parameter javaMethodParameter) {
		this(javaMethodParameter, javaMethodParameter.getDeclaringExecutable().getDeclaringClass());
	}

	public SpringWebJavaMethodParameterMetadata(java.lang.reflect.Parameter javaMethodParameter, Class<?> targetClassType) {
		isTrue(javaMethodParameter.getAnnotations().length <= 1, "Parameter " + javaMethodParameter + " has more than one annotation.");

		this.type = JavaType.of(new JavaTypeResolver(targetClassType).parameterizedTypeOf(javaMethodParameter));

		PathVariable pathParameter = Optional.ofNullable(javaMethodParameter.getAnnotation(PathVariable.class))
				.map(a -> AnnotationUtils.synthesizeAnnotation(a, javaMethodParameter))
					.orElse(null);

		RequestHeader headerParameter = Optional.ofNullable(javaMethodParameter.getAnnotation(RequestHeader.class))
				.map(a -> AnnotationUtils.synthesizeAnnotation(a, javaMethodParameter))
					.orElse(null);

		RequestBody bodyParameter = Optional.ofNullable(javaMethodParameter.getAnnotation(RequestBody.class))
				.map(a -> AnnotationUtils.synthesizeAnnotation(a, javaMethodParameter))
					.orElse(null);

		RequestParam queryParameter = Optional.ofNullable(javaMethodParameter.getAnnotation(RequestParam.class))
				.map(a -> AnnotationUtils.synthesizeAnnotation(a, javaMethodParameter))
					.orElse(null);

		CookieValue cookieParameter = Optional.ofNullable(javaMethodParameter.getAnnotation(CookieValue.class))
				.map(a -> AnnotationUtils.synthesizeAnnotation(a, javaMethodParameter))
					.orElse(null);

		this.annotationParameter = Arrays.asList(pathParameter, headerParameter, bodyParameter, queryParameter, cookieParameter)
				.stream().filter(a -> a != null).findFirst()
					.orElseThrow(()-> new IllegalArgumentException("The parameter [" + javaMethodParameter.getName() + "] of method [" + javaMethodParameter.getDeclaringExecutable() + "] isn't annotated with "
						+ "@PathVariable, @RequestHeader, @CookieValue, @RequestBody or @RequestParam"));

		String name = Optional.ofNullable(pathParameter)
				.map(PathVariable::value).filter(s -> !s.trim().isEmpty())
					.orElseGet(() -> Optional.ofNullable(headerParameter)
						.map(RequestHeader::value).filter(s -> !s.trim().isEmpty())
							.orElseGet(() -> Optional.ofNullable(queryParameter)
								.map(RequestParam::value).filter(s -> !s.trim().isEmpty())
									.orElseGet(() -> Optional.ofNullable(cookieParameter)
										.map(CookieValue::value).filter(s -> !s.trim().isEmpty())
											.orElseGet(() -> Optional.ofNullable(javaMethodParameter.getName())
												.filter(n -> javaMethodParameter.isNamePresent() && !n.isEmpty())
													.orElse(null)))));

		isFalse(needName() && name == null, "Could not get the name of the parameter [" + javaMethodParameter + "], "
				+ "from method [" + javaMethodParameter.getDeclaringExecutable() + "]");
		this.name = name;

		this.serializerType = SpringWebEndpointMethodParameterSerializer.of(this);
	}

	private boolean needName() {
		return annotationParameter instanceof PathVariable
			|| annotationParameter instanceof RequestParam
			|| annotationParameter instanceof RequestHeader
			|| annotationParameter instanceof CookieValue;
	}

	public String name() {
		return name;
	}

	public JavaType javaType() {
		return type;
	}

	public boolean path() {
		return annotationParameter instanceof PathVariable;
	}

	public boolean body() {
		return annotationParameter instanceof RequestBody;
	}

	public boolean header() {
		return annotationParameter instanceof RequestHeader;
	}

	public boolean cookie() {
		return annotationParameter instanceof CookieValue;
	}

	public boolean query() {
		return annotationParameter instanceof RequestParam;
	}

	public Class<? extends ParameterSerializer> serializer() {
		return serializerType;
	}
}
