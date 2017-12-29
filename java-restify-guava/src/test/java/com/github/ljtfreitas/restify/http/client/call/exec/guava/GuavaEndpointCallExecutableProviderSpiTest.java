package com.github.ljtfreitas.restify.http.client.call.exec.guava;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;

public class GuavaEndpointCallExecutableProviderSpiTest {

	@SuppressWarnings("unchecked")
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<EndpointCallExecutableProvider> services = ServiceLoader.load(EndpointCallExecutableProvider.class);

		assertThat(services, contains(
				instanceOf(ListenableFutureCallbackEndpointCallExecutableFactory.class),
				instanceOf(ListenableFutureEndpointCallExecutableFactory.class),
				instanceOf(ListenableFutureTaskEndpointCallExecutableFactory.class),
				instanceOf(OptionalEndpointCallExecutableFactory.class)));
	}
}
