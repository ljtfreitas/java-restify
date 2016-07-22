package com.restify.http.metadata;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.restify.http.contract.Path;
import com.restify.http.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.restify.http.metadata.reflection.JavaMethodMetadata;
import com.restify.http.metadata.reflection.JavaMethodParameterMetadata;
import com.restify.http.metadata.reflection.JavaTypeMetadata;

public class EndpointMethodReader {

	private final EndpointTarget target;

	public EndpointMethodReader(EndpointTarget target) {
		this.target = target;
	}

	public EndpointMethod read(java.lang.reflect.Method javaMethod) {
		JavaTypeMetadata javaTypeMetadata = new JavaTypeMetadata(target.type());

		JavaMethodMetadata javaMethodMetadata = new JavaMethodMetadata(javaMethod);

		String endpointPath = endpointTarget() + endpointTypePath(javaTypeMetadata) + endpointMethodPath(javaMethodMetadata);

		String endpointHttpMethod = javaMethodMetadata.httpMethod().value().toUpperCase();

		EndpointMethodParameters parameters = endpointMethodParameters(javaMethod);

		EndpointHeaders headers = endpointMethodHeaders(javaTypeMetadata, javaMethodMetadata);

		return new EndpointMethod(javaMethod, endpointPath, endpointHttpMethod, parameters, headers);
	}

	private String endpointTarget() {
		return target.endpoint().orElse("");
	}

	private String endpointTypePath(JavaTypeMetadata javaTypeMetadata) {
		return javaTypeMetadata.path()
				.map(Path::value)
				.map(p -> p.endsWith("/") ? p.substring(0, p.length() - 1) : p)
				.orElse("");
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
				javaMethodParameterMetadata.ofHeader()? EndpointMethodParameterType.HEADER : EndpointMethodParameterType.BODY;

			parameters.put(new EndpointMethodParameter(position, javaMethodParameterMetadata.name(), type));
		}

		return parameters;
	}

	private EndpointHeaders endpointMethodHeaders(JavaTypeMetadata javaTypeMetadata, JavaMethodMetadata javaMethodMetadata) {
		return new EndpointHeaders(
				Stream.concat(Arrays.stream(javaTypeMetadata.headers()), Arrays.stream(javaMethodMetadata.headers()))
					.map(h -> new EndpointHeader(h.name(), h.value()))
						.collect(Collectors.toSet()));
	}
}
