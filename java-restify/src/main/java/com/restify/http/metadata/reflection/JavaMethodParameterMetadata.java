package com.restify.http.metadata.reflection;

import java.lang.reflect.Parameter;
import java.util.Optional;

import com.restify.http.contract.BodyParameter;
import com.restify.http.contract.HeaderParameter;
import com.restify.http.contract.PathParameter;

public class JavaMethodParameterMetadata {

	private final String name;
	private final PathParameter pathParameter;
	private final HeaderParameter headerParameter;
	private final BodyParameter bodyParameter;

	public JavaMethodParameterMetadata(Parameter javaMethodParameter) {
		this.pathParameter = javaMethodParameter.getAnnotation(PathParameter.class);
		this.headerParameter = javaMethodParameter.getAnnotation(HeaderParameter.class);
		this.bodyParameter = javaMethodParameter.getAnnotation(BodyParameter.class);

		if ((pathParameter != null && headerParameter != null)
				|| (pathParameter != null && bodyParameter != null)
				|| (headerParameter != null && bodyParameter != null)) {

			throw new IllegalStateException("Parameter " + javaMethodParameter + " has more than one annotation.");
		}

		this.name = Optional.ofNullable(pathParameter)
				.map(PathParameter::value)
					.filter(s -> !s.trim().isEmpty())
						.orElseGet(() -> Optional.ofNullable(headerParameter)
							.map(HeaderParameter::value)
								.filter(s -> !s.trim().isEmpty())
									.orElseGet(() -> Optional.ofNullable(javaMethodParameter.getName())
										.orElseThrow(() -> new IllegalStateException("Could not get the name of the parameter " + javaMethodParameter))));

	}

	public String name() {
		return name;
	}

	public boolean ofPath() {
		return pathParameter != null || (headerParameter == null && bodyParameter == null);
	}

	public boolean ofBody() {
		return bodyParameter != null;
	}

	public boolean ofHeader() {
		return headerParameter != null;
	}

}
