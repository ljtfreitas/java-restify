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

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.ljtfreitas.restify.http.contract.ParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractExpressionResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractReader;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointHeader;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointHeaders;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethods;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointTarget;
import com.github.ljtfreitas.restify.http.contract.metadata.SimpleContractExpressionResolver;
import com.github.ljtfreitas.restify.util.Tryable;

public class SpringWebContractReader implements ContractReader {

	private final ContractExpressionResolver expressionsolver;

	public SpringWebContractReader() {
		this(new SimpleContractExpressionResolver());
	}

	public SpringWebContractReader(ContractExpressionResolver expressionResolver) {
		this.expressionsolver = expressionResolver;
	}

	@Override
	public EndpointMethods read(EndpointTarget target) {
		return new EndpointMethods(doRead(target));
	}

	private Collection<EndpointMethod> doRead(EndpointTarget target) {
		SpringWebJavaTypeMetadata javaTypeMetadata = new SpringWebJavaTypeMetadata(target.type());

		return target.methods().stream()
				.map(javaMethod -> doReadMethod(target, javaTypeMetadata, javaMethod))
					.collect(Collectors.toList());
	}

	private EndpointMethod doReadMethod(EndpointTarget target, SpringWebJavaTypeMetadata javaTypeMetadata, Method javaMethod) {
		SpringWebJavaMethodMetadata javaMethodMetadata = new SpringWebJavaMethodMetadata(javaMethod);

		String endpointPath = endpointPath(target, javaTypeMetadata, javaMethodMetadata);

		String endpointHttpMethod = javaMethodMetadata.httpMethod().name();

		EndpointMethodParameters parameters = endpointMethodParameters(javaMethod, target);

		EndpointHeaders headers = endpointMethodHeaders(javaTypeMetadata, javaMethodMetadata);

		Type returnType = javaMethodMetadata.returnType(target.type());

		return new EndpointMethod(javaMethod, endpointPath, endpointHttpMethod, parameters, headers, returnType);
	}

	private String endpointPath(EndpointTarget target, SpringWebJavaTypeMetadata javaTypeMetadata, SpringWebJavaMethodMetadata javaMethodMetadata) {
		String endpoint = expressionsolver.resolve(endpointTarget(target) + endpointTypePath(javaTypeMetadata) + 
				endpointMethodPath(javaMethodMetadata));

		return Tryable.of(() -> new URL(endpoint)).toString();
	}

	private String endpointTarget(EndpointTarget target) {
		return target.endpoint().orElse("");
	}

	private String endpointTypePath(SpringWebJavaTypeMetadata javaTypeMetadata) {
		return Arrays.stream(javaTypeMetadata.paths())
				.map(p -> p.endsWith("/") ? p.substring(0, p.length() - 1) : p)
				.collect(Collectors.joining());
	}

	private String endpointMethodPath(SpringWebJavaMethodMetadata javaMethodMetadata) {
		String endpointMethodPath = javaMethodMetadata.path().orElse("");
		return (endpointMethodPath.isEmpty() || endpointMethodPath.startsWith("/") ? endpointMethodPath
				: "/" + endpointMethodPath);
	}

	private EndpointMethodParameters endpointMethodParameters(Method javaMethod, EndpointTarget target) {
		EndpointMethodParameters parameters = new EndpointMethodParameters();

		Parameter[] javaMethodParameters = javaMethod.getParameters();

		for (int position = 0; position < javaMethodParameters.length; position ++) {
			Parameter javaMethodParameter = javaMethodParameters[position];

			SpringWebJavaMethodParameterMetadata javaMethodParameterMetadata = new SpringWebJavaMethodParameterMetadata(javaMethodParameter, target.type());

			EndpointMethodParameterType type = nonNull(javaMethodParameterMetadata.path()? EndpointMethodParameterType.PATH :
				javaMethodParameterMetadata.header()? EndpointMethodParameterType.HEADER :
					javaMethodParameterMetadata.body() ? EndpointMethodParameterType.BODY :
						javaMethodParameterMetadata.query() ? EndpointMethodParameterType.QUERY_STRING : null,
								"The parameter [" + javaMethodParameterMetadata.name() + "] of method [" + javaMethod + "] "
										+ "is not annotated with @PathVariable, @RequestHeader, @RequestBody or @RequestParam"); 

			ParameterSerializer serializer = serializerOf(javaMethodParameterMetadata);

			parameters.put(new EndpointMethodParameter(position, javaMethodParameterMetadata.name(), javaMethodParameterMetadata.javaType(), 
					type, serializer));
		}

		return parameters;
	}

	private ParameterSerializer serializerOf(SpringWebJavaMethodParameterMetadata javaMethodParameterMetadata) {
		try {
			return javaMethodParameterMetadata.serializer().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new UnsupportedOperationException("Cannot create new instance of EndpointMethodParameterSerializer type " + javaMethodParameterMetadata.serializer());
		}
	}

	private EndpointHeaders endpointMethodHeaders(SpringWebJavaTypeMetadata javaTypeMetadata,
			SpringWebJavaMethodMetadata javaMethodMetadata) {

			Collection<EndpointHeader> headers =
					Stream.concat(Arrays.stream(javaTypeMetadata.headers()), Arrays.stream(javaMethodMetadata.headers()))
						.map(h -> h.split("="))
							.filter(h -> h.length == 2)
								.map(h -> new EndpointHeader(h[0], expressionsolver.resolve(h[1])))
									.collect(Collectors.toCollection(LinkedHashSet::new));

			return new EndpointHeaders(headers);
	}
}
