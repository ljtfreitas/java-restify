package com.restify.http.contract.metadata.reflection;

import static com.restify.http.util.Preconditions.isTrue;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.stream.Stream;

import com.restify.http.contract.BodyParameter;
import com.restify.http.contract.CallbackParameter;
import com.restify.http.contract.HeaderParameter;
import com.restify.http.contract.Parameter;
import com.restify.http.contract.PathParameter;
import com.restify.http.contract.QueryParameter;
import com.restify.http.contract.QueryParameters;
import com.restify.http.contract.metadata.EndpointMethodParameterSerializer;
import com.restify.http.contract.metadata.SimpleEndpointMethodParameterSerializer;

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
					.count() <= 1, "Parameter " + javaMethodParameter + " has more than one annotation.");

		this.name = Optional.ofNullable(pathParameter)
				.map(PathParameter::value).filter(s -> !s.trim().isEmpty())
					.orElseGet(() -> Optional.ofNullable(headerParameter)
						.map(HeaderParameter::value).filter(s -> !s.trim().isEmpty())
							.orElseGet(() -> Optional.ofNullable(queryParameter)
								.map(QueryParameter::value).filter(s -> !s.trim().isEmpty())
									.orElseGet(() -> Optional.ofNullable(javaMethodParameter.getName())
											.orElseThrow(() -> new IllegalStateException("Could not get the name of the parameter " + javaMethodParameter)))));

		this.serializerType = pathParameter != null ? pathParameter.serializer()
				: queryParameter != null ? queryParameter.serializer()
						: queryParameters != null ? queryParameters.serializer()
								: callbackParameter != null ? null
										: SimpleEndpointMethodParameterSerializer.class;

		this.annotationParameter = new JavaAnnotationScanner(javaMethodParameter).with(Parameter.class);
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
