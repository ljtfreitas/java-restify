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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.ljtfreitas.restify.http.contract.Cookie;
import com.github.ljtfreitas.restify.http.contract.Header;
import com.github.ljtfreitas.restify.http.contract.ParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.Path;
import com.github.ljtfreitas.restify.http.contract.Version;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;

public class DefaultContractReader implements ContractReader {

	private final ContractExpressionResolver expressionResolver;

	public DefaultContractReader() {
		this(new SimpleContractExpressionResolver());
	}

	public DefaultContractReader(ContractExpressionResolver expressionResolver) {
		this.expressionResolver = expressionResolver;
	}

	@Override
	public EndpointMethods read(EndpointTarget target) {
		return new EndpointMethods(doRead(target));
	}

	private Collection<EndpointMethod> doRead(EndpointTarget target) {
		ContractTypeMetadata javaTypeMetadata = new ContractTypeMetadata(target.type());
		return target.methods().stream()
			.map(javaMethod -> doReadMethod(target, javaTypeMetadata, javaMethod))
				.collect(Collectors.toList());
	}

	private EndpointMethod doReadMethod(EndpointTarget target, ContractTypeMetadata javaTypeMetadata, java.lang.reflect.Method javaMethod) {
		ContractMethodMetadata javaMethodMetadata = new ContractMethodMetadata(javaMethod);

		String endpointPath = endpointPath(target, javaTypeMetadata, javaMethodMetadata);

		String endpointHttpMethod = javaMethodMetadata.httpMethod().value().toUpperCase();

		EndpointMethodParameters parameters = endpointMethodParameters(javaMethod, target);

		EndpointHeaders headers = endpointMethodHeaders(javaTypeMetadata, javaMethodMetadata);

		Type returnType = javaMethodMetadata.returnType(target.type());

		String version = endpointMethodVersion(endpointVersion(javaTypeMetadata, javaMethodMetadata, true));

		return new EndpointMethod(javaMethod, endpointPath, endpointHttpMethod, parameters, headers, returnType, version);
	}

	private String endpointPath(EndpointTarget target, ContractTypeMetadata javaTypeMetadata, ContractMethodMetadata javaMethodMetadata) {
		return new EndpointPathBuilder()
				.append(endpointTarget(target))
				.append(endpointTypePath(javaTypeMetadata))
				.append(endpointVersion(javaTypeMetadata, javaMethodMetadata))
				.append(endpointMethodPath(javaMethodMetadata))
				.build();
	}

	private String endpointTarget(EndpointTarget target) {
		return expressionResolver.resolve(target.endpoint().orElse(""));
	}

	private String endpointTypePath(ContractTypeMetadata javaTypeMetadata) {
		return Arrays.stream(javaTypeMetadata.paths())
				.map(Path::value)
					.map(p -> expressionResolver.resolve(p))
						.collect(Collectors.joining());
	}

	private String endpointVersion(ContractTypeMetadata javaTypeMetadata, ContractMethodMetadata javaMethodMetadata) {
		return endpointVersion(javaTypeMetadata, javaMethodMetadata, false);
	}

	private String endpointVersion(ContractTypeMetadata javaTypeMetadata, ContractMethodMetadata javaMethodMetadata, boolean force) {
		Version version = javaMethodMetadata.version()
			.filter(v -> v.uri() || force)
				.orElseGet(() -> javaTypeMetadata.version()
						.filter(v -> v.uri() || force)
							.orElse(null));

		return Optional.ofNullable(version)
			.map(Version::value)
				.orElse("");
	}

	private String endpointMethodVersion(String version) {
		 return Optional.ofNullable(version)
			.filter(v -> !v.trim().isEmpty())
				.orElse(null);
	}

	private String endpointMethodPath(ContractMethodMetadata javaMethodMetadata) {
		return javaMethodMetadata.path().map(Path::value).orElse("");
	}

	private EndpointMethodParameters endpointMethodParameters(Method javaMethod, EndpointTarget target) {
		EndpointMethodParameters parameters = new EndpointMethodParameters();

		Parameter[] javaMethodParameters = javaMethod.getParameters();

		for (int position = 0; position < javaMethodParameters.length; position ++) {
			Parameter javaMethodParameter = javaMethodParameters[position];

			ContractMethodParameterMetadata javaMethodParameterMetadata = new ContractMethodParameterMetadata(javaMethodParameter, target.type());

			EndpointMethodParameterType type = javaMethodParameterMetadata.path()? EndpointMethodParameterType.PATH :
				javaMethodParameterMetadata.header()? EndpointMethodParameterType.HEADER :
					javaMethodParameterMetadata.body() ? EndpointMethodParameterType.BODY :
						javaMethodParameterMetadata.query() ? EndpointMethodParameterType.QUERY_STRING :
							javaMethodParameterMetadata.cookie() ? EndpointMethodParameterType.COOKIE :
								EndpointMethodParameterType.ENDPOINT_CALLBACK;

			if (type == EndpointMethodParameterType.ENDPOINT_CALLBACK) {
				isTrue(parameters.callbacks(javaMethodParameterMetadata.javaType().classType()).isEmpty(),
						"Only one @CallbackParameter of type [" + javaMethodParameterMetadata.javaType() + "] is allowed.");
			}

			ParameterSerializer serializer = Optional.ofNullable(javaMethodParameterMetadata.serializer())
					.map(s -> serializerOf(javaMethodParameterMetadata)).orElse(null);

			parameters = parameters.put(new EndpointMethodParameter(position, javaMethodParameterMetadata.name(), javaMethodParameterMetadata.javaType(), type, serializer));
		}

		return parameters;
	}

	private ParameterSerializer serializerOf(ContractMethodParameterMetadata javaMethodParameterMetadata) {
		try {
			return javaMethodParameterMetadata.serializer().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new UnsupportedOperationException("Cannot create new instance of EndpointMethodParameterSerializer type " + javaMethodParameterMetadata.serializer());
		}
	}

	private EndpointHeaders endpointMethodHeaders(ContractTypeMetadata javaTypeMetadata, ContractMethodMetadata javaMethodMetadata) {
		Collection<Header> headersOfType = javaTypeMetadata.headers();
		Collection<Header> headersOfMethod = javaMethodMetadata.headers();

		Collection<EndpointHeader> headers = Stream.concat(headersOfType.stream(), headersOfMethod.stream())
				.map(h -> new EndpointHeader(h.name(), h.value()))
					.collect(Collectors.toCollection(HashSet::new));

		Collection<Cookie> cookiesOfType = javaTypeMetadata.cookies();
		Collection<Cookie> cookiesOfMethod = javaMethodMetadata.cookies();

		headers.addAll(Stream.concat(cookiesOfType.stream(), cookiesOfMethod.stream())
				.map(c -> new EndpointCookie(c.name(), c.value()))
					.map(EndpointCookie::asHeader)
						.collect(Collectors.toCollection(HashSet::new)));

		return new EndpointHeaders(headers);
	}
}
