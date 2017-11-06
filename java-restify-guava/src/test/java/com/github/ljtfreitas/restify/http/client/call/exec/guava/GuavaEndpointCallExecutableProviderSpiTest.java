package com.github.ljtfreitas.restify.http.client.call.exec.guava;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;
import com.github.ljtfreitas.restify.http.spi.ComponentLoader;

public class GuavaEndpointCallExecutableProviderSpiTest {

	private ComponentLoader loader = new ComponentLoader();

	@SuppressWarnings("unchecked")
	@Test
	public void shouldDiscoveryGuavaExecutableProviders() {
		Collection<EndpointCallExecutableProvider> services = loader.load(EndpointCallExecutableProvider.class);

		assertThat(services, contains(
				instanceOf(ListenableFutureCallbackEndpointCallExecutableFactory.class),
				instanceOf(ListenableFutureEndpointCallExecutableFactory.class),
				instanceOf(ListenableFutureTaskEndpointCallExecutableFactory.class),
				instanceOf(OptionalEndpointCallExecutableFactory.class)));
	}
}
