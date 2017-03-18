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

import static com.github.ljtfreitas.restify.http.util.Preconditions.isFalse;
import static com.github.ljtfreitas.restify.http.util.Preconditions.isTrue;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.ljtfreitas.restify.http.contract.BodyParameter;
import com.github.ljtfreitas.restify.http.contract.CallbackParameter;
import com.github.ljtfreitas.restify.http.contract.HeaderParameter;
import com.github.ljtfreitas.restify.http.contract.Parameter;
import com.github.ljtfreitas.restify.http.contract.PathParameter;
import com.github.ljtfreitas.restify.http.contract.QueryParameter;
import com.github.ljtfreitas.restify.http.contract.QueryParameters;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.metadata.SimpleEndpointMethodParameterSerializer;

public class JavaMethodParameterMetadata {

	private final JavaType type;
	private final String name;
	private final Annotation annotationParameter;
	private final Class<? extends EndpointMethodParameterSerializer> serializerType;

	public JavaMethodParameterMetadata(java.lang.reflect.Parameter javaMethodParameter) {
		this(javaMethodParameter, javaMethodParameter.getDeclaringExecutable().getDeclaringClass());
	}

	public JavaMethodParameterMetadata(java.lang.reflect.Parameter javaMethodParameter, Class<?> targetClassType) {
		this.type = JavaType.of(new JavaTypeResolver(targetClassType).parameterizedTypeOf(javaMethodParameter));

		PathParameter pathParameter = javaMethodParameter.getAnnotation(PathParameter.class);
		HeaderParameter headerParameter = javaMethodParameter.getAnnotation(HeaderParameter.class);
		QueryParameter queryParameter = javaMethodParameter.getAnnotation(QueryParameter.class);
		QueryParameters queryParameters = javaMethodParameter.getAnnotation(QueryParameters.class);
		CallbackParameter callbackParameter = javaMethodParameter.getAnnotation(CallbackParameter.class);

		isTrue(Stream.of(javaMethodParameter.getAnnotations())
				.filter(a -> a.annotationType().isAnnotationPresent(Parameter.class))
					.count() <= 1, "Parameter [" + javaMethodParameter + "], of method [" + javaMethodParameter.getDeclaringExecutable() + "] "
							+ "has more than one annotation.");

		this.annotationParameter = new JavaAnnotationScanner(javaMethodParameter).with(Parameter.class);

		String name = Optional.ofNullable(pathParameter)
				.map(PathParameter::value).filter(s -> !s.trim().isEmpty())
					.orElseGet(() -> Optional.ofNullable(headerParameter)
						.map(HeaderParameter::value).filter(s -> !s.trim().isEmpty())
							.orElseGet(() -> Optional.ofNullable(queryParameter)
								.map(QueryParameter::value).filter(s -> !s.trim().isEmpty())
									.orElseGet(() -> Optional.ofNullable(javaMethodParameter.getName())
											.filter(n -> javaMethodParameter.isNamePresent() && !n.isEmpty())
												.orElse(null))));

		isFalse(needName() && name == null, "Could not get the name of the parameter [" + javaMethodParameter + "], "
				+ "of method [" + javaMethodParameter.getDeclaringExecutable() + "]");
		this.name = name;

		this.serializerType = pathParameter != null ? pathParameter.serializer()
				: queryParameter != null ? queryParameter.serializer()
						: queryParameters != null ? queryParameters.serializer()
								: callbackParameter != null ? null
										: SimpleEndpointMethodParameterSerializer.class;
	}

	private boolean needName() {
		return (annotationParameter instanceof PathParameter || annotationParameter == null)
			|| annotationParameter instanceof QueryParameter
			|| annotationParameter instanceof HeaderParameter;
	}

	public String name() {
		return name;
	}

	public JavaType javaType() {
		return type;
	}

	public boolean path() {
		return annotationParameter instanceof PathParameter || annotationParameter == null;
	}

	public boolean body() {
		return annotationParameter instanceof BodyParameter;
	}

	public boolean header() {
		return annotationParameter instanceof HeaderParameter;
	}

	public boolean query() {
		return annotationParameter instanceof QueryParameter || annotationParameter instanceof QueryParameters;
	}

	public boolean callback() {
		return annotationParameter instanceof CallbackParameter;
	}

	public Class<? extends EndpointMethodParameterSerializer> serializer() {
		return serializerType;
	}
}
