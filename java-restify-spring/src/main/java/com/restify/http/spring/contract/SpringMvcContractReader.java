package com.restify.http.spring.contract;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.restify.http.contract.metadata.EndpointHeader;
import com.restify.http.contract.metadata.EndpointHeaders;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.EndpointMethodParameter;
import com.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.restify.http.contract.metadata.EndpointMethodParameterSerializer;
import com.restify.http.contract.metadata.EndpointMethodParameters;
import com.restify.http.contract.metadata.EndpointTarget;
import com.restify.http.contract.metadata.RestifyContractReader;
import com.restify.http.spring.contract.metadata.SpringDynamicParameterExpressionResolver;
import com.restify.http.spring.contract.metadata.SimpleSpringDynamicParameterExpressionResolver;
import com.restify.http.spring.contract.metadata.reflection.SpringMvcJavaMethodMetadata;
import com.restify.http.spring.contract.metadata.reflection.SpringMvcJavaMethodParameterMetadata;
import com.restify.http.spring.contract.metadata.reflection.SpringMvcJavaTypeMetadata;

public class SpringMvcContractReader implements RestifyContractReader {

	private final SpringDynamicParameterExpressionResolver resolver;

	public SpringMvcContractReader() {
		this(new SimpleSpringDynamicParameterExpressionResolver());
	}

	public SpringMvcContractReader(SpringDynamicParameterExpressionResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public EndpointMethod read(EndpointTarget target, Method javaMethod) {
		SpringMvcJavaTypeMetadata javaTypeMetadata = new SpringMvcJavaTypeMetadata(target.type());

		SpringMvcJavaMethodMetadata javaMethodMetadata = new SpringMvcJavaMethodMetadata(javaMethod);

		String endpointPath = resolver.resolve(endpointTarget(target) + endpointTypePath(javaTypeMetadata)
				+ endpointMethodPath(javaMethodMetadata));

		String endpointHttpMethod = javaMethodMetadata.httpMethod().name();

		EndpointMethodParameters parameters = endpointMethodParameters(javaMethod);

		EndpointHeaders headers = endpointMethodHeaders(javaTypeMetadata, javaMethodMetadata);

		Type returnType = javaMethodMetadata.returnType(target.type());

		return new EndpointMethod(javaMethod, endpointPath, endpointHttpMethod, parameters, headers, returnType);
	}

	private String endpointTarget(EndpointTarget target) {
		return target.endpoint().orElse("");
	}

	private String endpointTypePath(SpringMvcJavaTypeMetadata javaTypeMetadata) {
		return Arrays.stream(javaTypeMetadata.paths())
				.map(p -> p.endsWith("/") ? p.substring(0, p.length() - 1) : p)
				.collect(Collectors.joining());
	}

	private String endpointMethodPath(SpringMvcJavaMethodMetadata javaMethodMetadata) {
		String endpointMethodPath = javaMethodMetadata.path().orElse("");
		return (endpointMethodPath.isEmpty() || endpointMethodPath.startsWith("/") ? endpointMethodPath
				: "/" + endpointMethodPath);
	}

	private EndpointMethodParameters endpointMethodParameters(Method javaMethod) {
		EndpointMethodParameters parameters = new EndpointMethodParameters();

		Parameter[] javaMethodParameters = javaMethod.getParameters();

		for (int position = 0; position < javaMethodParameters.length; position ++) {
			Parameter javaMethodParameter = javaMethodParameters[position];

			SpringMvcJavaMethodParameterMetadata javaMethodParameterMetadata = new SpringMvcJavaMethodParameterMetadata(javaMethodParameter);

			EndpointMethodParameterType type = javaMethodParameterMetadata.ofPath()? EndpointMethodParameterType.PATH :
				javaMethodParameterMetadata.ofHeader()? EndpointMethodParameterType.HEADER :
					javaMethodParameterMetadata.ofBody() ? EndpointMethodParameterType.BODY :
						EndpointMethodParameterType.QUERY_STRING;

			EndpointMethodParameterSerializer serializer = serializerOf(javaMethodParameterMetadata);

			parameters.put(new EndpointMethodParameter(position, javaMethodParameterMetadata.name(), javaMethodParameterMetadata.javaType(), 
					type, serializer));
		}

		return parameters;
	}

	private EndpointMethodParameterSerializer serializerOf(SpringMvcJavaMethodParameterMetadata javaMethodParameterMetadata) {
		try {
			return javaMethodParameterMetadata.serializer().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new UnsupportedOperationException("Cannot create new instance of EndpointMethodParameterSerializer type " + javaMethodParameterMetadata.serializer());
		}
	}

	private EndpointHeaders endpointMethodHeaders(SpringMvcJavaTypeMetadata javaTypeMetadata,
			SpringMvcJavaMethodMetadata javaMethodMetadata) {

			Collection<EndpointHeader> headers =
					Stream.concat(Arrays.stream(javaTypeMetadata.headers()), Arrays.stream(javaMethodMetadata.headers()))
						.map(h -> h.split("="))
							.filter(h -> h.length == 2)
								.map(h -> new EndpointHeader(h[0], resolver.resolve(h[1])))
									.collect(Collectors.toCollection(LinkedHashSet::new));

			return new EndpointHeaders(headers);
	}
}
