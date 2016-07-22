package com.restify.http.metadata;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EndpointMethods {

	private final Map<Method, EndpointMethod> methods = new HashMap<>();

	public EndpointMethods(Collection<EndpointMethod> endpointMethods) {
		endpointMethods.stream().forEach(m -> methods.put(m.javaMethod(), m));
	}

	public Optional<EndpointMethod> find(Method method) {
		return Optional.ofNullable(methods.get(method));
	}

	@Override
	public String toString() {
		return methods.values().toString();
	}
}
