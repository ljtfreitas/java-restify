package com.restify.http.contract.metadata;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.restify.http.contract.Path;
import com.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.restify.http.contract.metadata.reflection.JavaMethodMetadata;
import com.restify.http.contract.metadata.reflection.JavaMethodParameterMetadata;
import com.restify.http.contract.metadata.reflection.JavaTypeMetadata;

public class DefaultRestifyContractReader implements RestifyContractReader {

	@Override
	public EndpointMethod read(EndpointTarget target, java.lang.reflect.Method javaMethod) {
		JavaTypeMetadata javaTypeMetadata = new JavaTypeMetadata(target.type());

		JavaMethodMetadata javaMethodMetadata = new JavaMethodMetadata(javaMethod);

		String endpointPath = endpointTarget(target) + endpointTypePath(javaTypeMetadata) + endpointMethodPath(javaMethodMetadata);

		String endpointHttpMethod = javaMethodMetadata.httpMethod().value().toUpperCase();

		EndpointMethodParameters parameters = endpointMethodParameters(javaMethod);

		EndpointHeaders headers = endpointMethodHeaders(javaTypeMetadata, javaMethodMetadata);

		Type returnType = javaMethodMetadata.returnType(target.type());

		return new EndpointMethod(javaMethod, endpointPath, endpointHttpMethod, parameters, headers, returnType);
	}

	private String endpointTarget(EndpointTarget target) {
		return target.endpoint().orElse("");
	}

	private String endpointTypePath(JavaTypeMetadata javaTypeMetadata) {
		return Arrays.stream(javaTypeMetadata.paths())
				.map(Path::value)
					.map(p -> p.endsWith("/") ? p.substring(0, p.length() - 1) : p)
						.collect(Collectors.joining());
	}

	private String endpointMethodPath(JavaMethodMetadata javaMethodMetadata) {
		String endpointMethodPath = javaMethodMetadata.path().value();
		return (endpointMethodPath.startsWith("/") ? endpointMethodPath : "/" + endpointMethodPath);
	}

	private EndpointMethodParameters endpointMethodParameters(Method javaMethod) {
		EndpointMethodParameters parameters = new EndpointMethodParameters();

		Parameter[] javaMethodParameters = javaMethod.getParameters();

		for (int position = 0; position < javaMethodParameters.length; position ++) {
			Parameter javaMethodParameter = javaMethodParameters[position];

			JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethodParameter);

			EndpointMethodParameterType type = javaMethodParameterMetadata.ofPath()? EndpointMethodParameterType.PATH :
				javaMethodParameterMetadata.ofHeader()? EndpointMethodParameterType.HEADER :
					javaMethodParameterMetadata.ofBody() ? EndpointMethodParameterType.BODY :
						EndpointMethodParameterType.QUERY_STRING;

			EndpointMethodParameterSerializer serializer = serializerOf(javaMethodParameterMetadata);

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
