package com.restify.http.contract;

import static com.restify.http.util.Preconditions.nonNull;

import java.util.Collection;
import java.util.stream.Collectors;

import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.EndpointMethods;
import com.restify.http.contract.metadata.EndpointTarget;
import com.restify.http.contract.metadata.EndpointType;
import com.restify.http.contract.metadata.RestifyContractReader;

public class DefaultRestifyContract implements RestifyContract {

	private final RestifyContractReader reader;

	public DefaultRestifyContract(RestifyContractReader reader) {
		this.reader = reader;
	}

	@Override
	public EndpointType read(EndpointTarget target) {
		nonNull(target, "Endpoint target cannot be null.");

		Collection<EndpointMethod> endpointMethods = target.methods().stream()
					.map(javaMethod -> reader.read(target, javaMethod))
						.collect(Collectors.toSet());

		return new EndpointType(target, new EndpointMethods(endpointMethods));
	}
}
