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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.github.ljtfreitas.restify.http.contract.ParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.QueryParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.SimpleParameterSerializer;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.JavaTypeResolver;

class JaxRsJavaMethodParameterMetadata {

	private final JavaType type;
	private final String name;
	private final Annotation annotationParameter;
	private final ParameterSerializer serializer;

	public JaxRsJavaMethodParameterMetadata(java.lang.reflect.Parameter javaMethodParameter, Class<?> targetClassType) {
		this.type = JavaType.of(new JavaTypeResolver(targetClassType).parameterizedTypeOf(javaMethodParameter));

		PathParam pathParameter = javaMethodParameter.getAnnotation(PathParam.class);
		HeaderParam headerParameter = javaMethodParameter.getAnnotation(HeaderParam.class);
		QueryParam queryParameter = javaMethodParameter.getAnnotation(QueryParam.class);

		isTrue(javaMethodParameter.getAnnotations().length <= 1, "Parameter " + javaMethodParameter + " has more than one annotation.");

		this.annotationParameter = Arrays.asList(pathParameter, headerParameter, queryParameter)
				.stream().filter(a -> a != null).findFirst().orElse(null);

		String name = Optional.ofNullable(pathParameter)
				.map(PathParam::value).filter(s -> !s.trim().isEmpty())
					.orElseGet(() -> Optional.ofNullable(headerParameter)
						.map(HeaderParam::value).filter(s -> !s.trim().isEmpty())
							.orElseGet(() -> Optional.ofNullable(queryParameter)
								.map(QueryParam::value).filter(s -> !s.trim().isEmpty())
									.orElseGet(() -> Optional.ofNullable(javaMethodParameter.getName())
											.filter(n -> javaMethodParameter.isNamePresent() && !n.isEmpty())
												.orElseGet(() -> "unknown"))));

		isFalse(needName() && name == null, "Could not get the name of the parameter [" + javaMethodParameter + "], "
				+ "from method [" + javaMethodParameter.getDeclaringExecutable() + "]");
		this.name = name;

		this.serializer = (queryParameter != null) ? new QueryParameterSerializer()
				: new SimpleParameterSerializer();
	}

	private boolean needName() {
		return annotationParameter instanceof PathParam
			|| annotationParameter instanceof QueryParam
			|| annotationParameter instanceof HeaderParam;
	}

	public String name() {
		return name;
	}

	public JavaType javaType() {
		return type;
	}

	public boolean path() {
		return annotationParameter instanceof PathParam;
	}

	public boolean body() {
		return annotationParameter == null;
	}

	public boolean header() {
		return annotationParameter instanceof HeaderParam;
	}

	public boolean query() {
		return annotationParameter instanceof QueryParam;
	}

	public ParameterSerializer serializer() {
		return serializer;
	}

	@SuppressWarnings("unchecked")
	public <A extends Annotation> A annotation(Class<A> annotationType) {
		return (A) annotationParameter;
	}
}
