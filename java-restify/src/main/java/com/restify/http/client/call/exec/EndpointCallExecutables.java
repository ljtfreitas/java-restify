package com.restify.http.client.call.exec;

import java.util.ArrayList;
import java.util.Collection;

import com.restify.http.contract.metadata.EndpointMethod;

public class EndpointCallExecutables {

	private static final EndpointCallExecutableFactory<? super Object, ? super Object> DEFAULT_EXECUTABLE_FACTORY
		= new DefaultEndpointCallExecutableFactory<>();

	private final Collection<EndpointCallExecutableProvider> providers;

	public EndpointCallExecutables(Collection<EndpointCallExecutableProvider> providers) {
		this.providers = new ArrayList<>(providers);
	}

	public <M, T> EndpointCallExecutable<M, T> of(EndpointMethod endpointMethod) {
		EndpointCallExecutableProvider provider = providerTo(endpointMethod);
		return decorator(provider) ? decorate(endpointMethod, provider) : create(endpointMethod, provider);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <M, T> EndpointCallExecutable<M, T> create(EndpointMethod endpointMethod, EndpointCallExecutableProvider provider) {
		EndpointCallExecutableFactory<M, T> factory = (EndpointCallExecutableFactory) provider;
		return factory.create(endpointMethod);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <M, T, O> EndpointCallExecutable<M, O> decorate(EndpointMethod endpointMethod, EndpointCallExecutableProvider provider) {
		EndpointCallExecutableDecoratorFactory<M, T, O> decorator = (EndpointCallExecutableDecoratorFactory) provider;
		return decorator.create(endpointMethod, of(endpointMethod.with(decorator.returnType(endpointMethod))));
	}

	private boolean decorator(EndpointCallExecutableProvider provider) {
		return provider instanceof EndpointCallExecutableDecoratorFactory;
	}

	private EndpointCallExecutableProvider providerTo(EndpointMethod endpointMethod) {
		return providers.stream()
					.filter(f -> f.supports(endpointMethod))
						.findFirst()
							.orElseGet(() -> DEFAULT_EXECUTABLE_FACTORY);
	}
}
