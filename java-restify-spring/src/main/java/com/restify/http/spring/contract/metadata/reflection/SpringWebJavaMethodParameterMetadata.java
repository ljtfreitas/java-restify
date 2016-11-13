package com.restify.http.spring.contract.metadata.reflection;

import java.util.Optional;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.restify.http.contract.metadata.EndpointMethodParameterSerializer;
import com.restify.http.contract.metadata.reflection.JavaType;
import com.restify.http.contract.metadata.reflection.JavaTypeResolver;
import com.restify.http.spring.contract.metadata.SpringWebEndpointMethodParameterSerializer;

public class SpringWebJavaMethodParameterMetadata {

	private final JavaType type;
	private final String name;
	private final PathVariable pathParameter;
	private final RequestHeader headerParameter;
	private final RequestBody bodyParameter;
	private final RequestParam queryParameter;
	private final Class<? extends EndpointMethodParameterSerializer> serializerType;

	public SpringWebJavaMethodParameterMetadata(java.lang.reflect.Parameter javaMethodParameter) {
		this(javaMethodParameter, javaMethodParameter.getDeclaringExecutable().getDeclaringClass());
	}

	public SpringWebJavaMethodParameterMetadata(java.lang.reflect.Parameter javaMethodParameter, Class<?> targetClassType) {
		this.type = JavaType.of(new JavaTypeResolver(targetClassType).parameterizedTypeOf(javaMethodParameter));

		this.pathParameter = AnnotationUtils.synthesizeAnnotation(javaMethodParameter.getAnnotation(PathVariable.class),
				javaMethodParameter);

		this.headerParameter = AnnotationUtils.synthesizeAnnotation(javaMethodParameter.getAnnotation(RequestHeader.class),
				javaMethodParameter);

		this.bodyParameter = AnnotationUtils.synthesizeAnnotation(javaMethodParameter.getAnnotation(RequestBody.class),
				javaMethodParameter);

		this.queryParameter = AnnotationUtils.synthesizeAnnotation(javaMethodParameter.getAnnotation(RequestParam.class),
				javaMethodParameter);

		this.name = Optional.ofNullable(pathParameter)
				.map(PathVariable::value).filter(s -> !s.trim().isEmpty())
					.orElseGet(() -> Optional.ofNullable(headerParameter)
						.map(RequestHeader::value).filter(s -> !s.trim().isEmpty())
							.orElseGet(() -> Optional.ofNullable(queryParameter)
								.map(RequestParam::value).filter(s -> !s.trim().isEmpty())
									.orElseGet(() -> Optional.ofNullable(javaMethodParameter.getName())
											.orElseThrow(() -> new IllegalStateException("Could not get the name of the parameter " + javaMethodParameter)))));

		this.serializerType = SpringWebEndpointMethodParameterSerializer.of(this);
	}

	public String name() {
		return name;
	}

	public JavaType javaType() {
		return type;
	}

	public boolean path() {
		return pathParameter != null;
	}

	public boolean body() {
		return bodyParameter != null;
	}

	public boolean header() {
		return headerParameter != null;
	}

	public boolean query() {
		return queryParameter != null;
	}

	public Class<? extends EndpointMethodParameterSerializer> serializer() {
		return serializerType;
	}
}
