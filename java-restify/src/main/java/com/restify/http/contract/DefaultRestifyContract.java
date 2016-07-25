package com.restify.http.contract;

import static com.restify.http.metadata.Preconditions.nonNull;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.restify.http.metadata.EndpointMethodReader;
import com.restify.http.metadata.EndpointMethods;
import com.restify.http.metadata.EndpointTarget;
import com.restify.http.metadata.EndpointType;

public class DefaultRestifyContract implements RestifyContract {

	@Override
	public EndpointType read(EndpointTarget target) {
		nonNull(target, "Endpoint target cannot be null.");

		Class<?> javaType = target.type();

		EndpointMethods endpointMethods = new EndpointMethods(
				Arrays.stream(javaType.getDeclaredMethods())
					.filter(m -> !m.isDefault() && !Modifier.isStatic(m.getModifiers()))
					.map(javaMethod -> new EndpointMethodReader(target).read(javaMethod))
						.collect(Collectors.toSet()));

		return new EndpointType(target, endpointMethods);
	}

}
