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

import static com.github.ljtfreitas.restify.http.util.Preconditions.isTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.ljtfreitas.restify.http.contract.Path;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaMethodMetadata;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaMethodParameterMetadata;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaTypeMetadata;

public class DefaultRestifyContractReader implements RestifyContractReader {

	private final RestifyContractExpressionResolver expressionResolver;

	public DefaultRestifyContractReader() {
		this(new SimpleRestifyContractExpressionResolver());
	}

	public DefaultRestifyContractReader(RestifyContractExpressionResolver expressionResolver) {
		this.expressionResolver = expressionResolver;
	}

	@Override
	public EndpointMethod read(EndpointTarget target, java.lang.reflect.Method javaMethod) {
		JavaTypeMetadata javaTypeMetadata = new JavaTypeMetadata(target.type());

		JavaMethodMetadata javaMethodMetadata = new JavaMethodMetadata(javaMethod);

		String endpointPath = endpointPath(target, javaTypeMetadata, javaMethodMetadata);

		String endpointHttpMethod = javaMethodMetadata.httpMethod().value().toUpperCase();

		EndpointMethodParameters parameters = endpointMethodParameters(javaMethod, target);

		EndpointHeaders headers = endpointMethodHeaders(javaTypeMetadata, javaMethodMetadata);

		Type returnType = javaMethodMetadata.returnType(target.type());

		return new EndpointMethod(javaMethod, endpointPath, endpointHttpMethod, parameters, headers, returnType);
	}

	private String endpointPath(EndpointTarget target, JavaTypeMetadata javaTypeMetadata, JavaMethodMetadata javaMethodMetadata) {
		return endpointTarget(target) + endpointTypePath(javaTypeMetadata) + endpointMethodPath(javaMethodMetadata);
	}

	private String endpointTarget(EndpointTarget target) {
		String endpoint = expressionResolver.resolve(target.endpoint().orElse(""));
		return endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
	}

	private String endpointTypePath(JavaTypeMetadata javaTypeMetadata) {
		return Arrays.stream(javaTypeMetadata.paths())
				.map(Path::value)
					.map(p -> expressionResolver.resolve(p))
						.map(p -> p.endsWith("/") ? p.substring(0, p.length() - 1) : p)
							.collect(Collectors.joining());
	}

	private String endpointMethodPath(JavaMethodMetadata javaMethodMetadata) {
		String endpointMethodPath = javaMethodMetadata.path().value();
		return (endpointMethodPath.startsWith("/") ? endpointMethodPath : "/" + endpointMethodPath);
	}

	private EndpointMethodParameters endpointMethodParameters(Method javaMethod, EndpointTarget target) {
		EndpointMethodParameters parameters = new EndpointMethodParameters();

		Parameter[] javaMethodParameters = javaMethod.getParameters();

		for (int position = 0; position < javaMethodParameters.length; position ++) {
			Parameter javaMethodParameter = javaMethodParameters[position];

			JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethodParameter, target.type());

			EndpointMethodParameterType type = javaMethodParameterMetadata.path()? EndpointMethodParameterType.PATH :
				javaMethodParameterMetadata.header()? EndpointMethodParameterType.HEADER :
					javaMethodParameterMetadata.body() ? EndpointMethodParameterType.BODY :
						javaMethodParameterMetadata.query() ? EndpointMethodParameterType.QUERY_STRING :
							EndpointMethodParameterType.ENDPOINT_CALLBACK;

			if (type == EndpointMethodParameterType.ENDPOINT_CALLBACK) {
				isTrue(parameters.callbacks(javaMethodParameterMetadata.javaType().classType()).isEmpty(),
						"Only one @CallbackParameter of type [" + javaMethodParameterMetadata.javaType() + "] is allowed.");
			}

			EndpointMethodParameterSerializer serializer = Optional.ofNullable(javaMethodParameterMetadata.serializer())
					.map(s -> serializerOf(javaMethodParameterMetadata)).orElse(null);

			parameters.put(new EndpointMethodParameter(position, javaMethodParameterMetadata.name(), javaMethodParameterMetadata.javaType(), type, serializer));
		}

		return parameters;
	}

	private EndpointMethodParameterSerializer serializerOf(JavaMethodParameterMetadata javaMethodParameterMetadata) {
		try {
			return javaMethodParameterMetadata.serializer().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new UnsupportedOperationException("Cannot create new instance of EndpointMethodParameterSerializer type " + javaMethodParameterMetadata.serializer());
		}
	}

	private EndpointHeaders endpointMethodHeaders(JavaTypeMetadata javaTypeMetadata, JavaMethodMetadata javaMethodMetadata) {
		return new EndpointHeaders(
				Stream.concat(Arrays.stream(javaTypeMetadata.headers()), Arrays.stream(javaMethodMetadata.headers()))
					.map(h -> new EndpointHeader(h.name(), h.value()))
						.collect(Collectors.toSet()));
	}
}
