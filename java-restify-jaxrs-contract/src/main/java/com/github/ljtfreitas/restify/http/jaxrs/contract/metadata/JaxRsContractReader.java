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

import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.github.ljtfreitas.restify.http.contract.ParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractReader;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointHeader;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointHeaders;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethods;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointTarget;
import com.github.ljtfreitas.restify.util.Tryable;

public class JaxRsContractReader implements ContractReader {

	@Override
	public EndpointMethods read(EndpointTarget endpointTarget) {
		return new EndpointMethods(doRead(endpointTarget));
	}

	private Collection<EndpointMethod> doRead(EndpointTarget target) {
		JaxRsJavaTypeMetadata javaTypeMetadata = new JaxRsJavaTypeMetadata(target.type());

		return target.methods().stream()
				.map(javaMethod -> doReadMethod(target, javaTypeMetadata, javaMethod))
					.collect(Collectors.toList());
	}

	private EndpointMethod doReadMethod(EndpointTarget target, JaxRsJavaTypeMetadata javaTypeMetadata, java.lang.reflect.Method javaMethod) {
		JaxRsJavaMethodMetadata javaMethodMetadata = new JaxRsJavaMethodMetadata(javaMethod);

		JaxRsJavaMethodParameters javaMethodParameters = new JaxRsJavaMethodParameters(javaMethod, target.type());

		String endpointPath = endpointPath(target, javaTypeMetadata, javaMethodMetadata);

		String endpointHttpMethod = javaMethodMetadata.httpMethod().value().toUpperCase();

		EndpointMethodParameters parameters = endpointMethodParameters(javaMethodParameters);

		EndpointHeaders headers = endpointMethodHeaders(javaTypeMetadata, javaMethodMetadata, javaMethodParameters);

		Type returnType = javaMethodMetadata.returnType(target.type());

		return new EndpointMethod(javaMethod, endpointPath, endpointHttpMethod, parameters, headers, returnType);
	}

	private String endpointPath(EndpointTarget target, JaxRsJavaTypeMetadata javaTypeMetadata, JaxRsJavaMethodMetadata javaMethodMetadata) {
		String endpoint = endpointTarget(target) + endpointTypePath(javaTypeMetadata) + endpointMethodPath(javaMethodMetadata);

		return Tryable.of(() -> new URL(endpoint)).toString();
	}

	private String endpointTarget(EndpointTarget target) {
		return target.endpoint().orElse("");
	}

	private String endpointTypePath(JaxRsJavaTypeMetadata javaTypeMetadata) {
		return Arrays.stream(javaTypeMetadata.paths())
				.map(p -> p.endsWith("/") ? p.substring(0, p.length() - 1) : p)
					.collect(Collectors.joining());
	}

	private String endpointMethodPath(JaxRsJavaMethodMetadata javaMethodMetadata) {
		String endpointMethodPath = javaMethodMetadata.path().map(Path::value).orElse("");
		return (endpointMethodPath.isEmpty() || endpointMethodPath.startsWith("/") ? endpointMethodPath : "/" + endpointMethodPath);
	}

	private EndpointMethodParameters endpointMethodParameters(JaxRsJavaMethodParameters javaMethodParameters) {
		EndpointMethodParameters parameters = new EndpointMethodParameters();

		for (int position = 0; position < javaMethodParameters.size(); position ++) {
			JaxRsJavaMethodParameterMetadata javaMethodParameterMetadata = javaMethodParameters.get(position);

			EndpointMethodParameterType type = javaMethodParameterMetadata.path() ? EndpointMethodParameterType.PATH :
				javaMethodParameterMetadata.header() ? EndpointMethodParameterType.HEADER :
					javaMethodParameterMetadata.query() ? EndpointMethodParameterType.QUERY_STRING :
						EndpointMethodParameterType.BODY;

			ParameterSerializer serializer = javaMethodParameterMetadata.serializer();

			parameters.put(new EndpointMethodParameter(position, javaMethodParameterMetadata.name(), javaMethodParameterMetadata.javaType(), type, serializer));
		}

		return parameters;
	}

	private EndpointHeaders endpointMethodHeaders(JaxRsJavaTypeMetadata javaTypeMetadata, JaxRsJavaMethodMetadata javaMethodMetadata, 
			JaxRsJavaMethodParameters javaMethodParameters) {

		Collection<JaxRsEndpointHeader> headers = new LinkedHashSet<>();
		headers.addAll(Arrays.asList(javaTypeMetadata.headers()));
		headers.addAll(Arrays.asList(javaMethodMetadata.headers()));
		headers.addAll(Arrays.asList(javaMethodParameters.headers()));

		return new EndpointHeaders(headers.stream().map(h -> new EndpointHeader(h.name(), h.value())).collect(Collectors.toSet()));
	}
}
