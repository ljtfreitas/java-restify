package com.restify.http.client.call.exec;

import java.util.ArrayList;
import java.util.Collection;

import com.restify.http.contract.metadata.EndpointMethod;

public class EndpointCallExecutables {

	private static final EndpointCallExecutableFactory<? super Object, ? super Object> DEFAULT_EXECUTABLE_FACTORY
		= new DefaultEndpointCallExecutableFactory<>();

	private final Collection<EndpointCallExecutableFactory<?, ?>> factories;

	public EndpointCallExecutables(Collection<EndpointCallExecutableFactory<?, ?>> factories) {
		this.factories = new ArrayList<>(factories);
	}

	@SuppressWarnings("unchecked")
	public <M, T> EndpointCallExecutable<M, T> of(EndpointMethod endpointMethod) {
		return (EndpointCallExecutable<M, T>) factories.stream()
					.filter(f -> f.supports(endpointMethod))
						.findFirst()
							.orElseGet(() -> DEFAULT_EXECUTABLE_FACTORY)
								.create(endpointMethod);
	}
}
