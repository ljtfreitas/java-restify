package com.restify.http.contract;

import static com.restify.http.util.Preconditions.nonNull;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.restify.http.contract.metadata.EndpointMethodReader;
import com.restify.http.contract.metadata.EndpointMethods;
import com.restify.http.contract.metadata.EndpointTarget;
import com.restify.http.contract.metadata.EndpointType;

public class DefaultRestifyContract implements RestifyContract {

	@Override
	public EndpointType read(EndpointTarget target) {
		nonNull(target, "Endpoint target cannot be null.");

		Class<?> javaType = target.type();

		EndpointMethods endpointMethods = new EndpointMethods(
				Arrays.stream(javaType.getMethods())
					.filter(javaMethod -> javaMethod.getDeclaringClass() != Object.class 
						|| !javaMethod.isDefault() 
						|| !Modifier.isStatic(javaMethod.getModifiers()))
					.map(javaMethod -> new EndpointMethodReader(target).read(javaMethod))
						.collect(Collectors.toSet()));

		return new EndpointType(target, endpointMethods);
	}

}
