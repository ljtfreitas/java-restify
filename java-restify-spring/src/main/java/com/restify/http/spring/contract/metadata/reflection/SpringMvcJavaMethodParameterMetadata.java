package com.restify.http.spring.contract.metadata.reflection;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Optional;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.restify.http.contract.metadata.EndpointMethodParameterSerializer;
import com.restify.http.spring.contract.metadata.SpringMvcEndpointMethodParameterSerializer;

public class SpringMvcJavaMethodParameterMetadata {

	private final Parameter javaMethodParameter;
	private final String name;
	private final PathVariable pathParameter;
	private final RequestHeader headerParameter;
	private final RequestBody bodyParameter;
	private final RequestParam queryParameter;
	private final Class<? extends EndpointMethodParameterSerializer> serializerType;

	public SpringMvcJavaMethodParameterMetadata(Parameter javaMethodParameter) {
		this.javaMethodParameter = javaMethodParameter;

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

		this.serializerType = SpringMvcEndpointMethodParameterSerializer.of(this);
	}

	public String name() {
		return name;
	}

	public Type javaType() {
		return javaMethodParameter.getParameterizedType();
	}

	public boolean ofPath() {
		return pathParameter != null || (headerParameter == null && bodyParameter == null && queryParameter == null);
	}

	public boolean ofBody() {
		return bodyParameter != null;
	}

	public boolean ofHeader() {
		return headerParameter != null;
	}

	public boolean ofQuery() {
		return queryParameter != null;
	}

	public Class<? extends EndpointMethodParameterSerializer> serializer() {
		return serializerType;
	}
}
