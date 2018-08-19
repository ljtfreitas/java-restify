package com.github.ljtfreitas.restify.http.client.call.handler.guava;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerProvider;

public class GuavaEndpointCallHandlerProviderSpiTest {

	@SuppressWarnings("unchecked")
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<EndpointCallHandlerProvider> services = ServiceLoader.load(EndpointCallHandlerProvider.class);

		assertThat(services, contains(
				instanceOf(ListenableFutureCallbackEndpointCallHandlerAdapter.class),
				instanceOf(ListenableFutureEndpointCallHandlerAdapter.class),
				instanceOf(ListenableFutureTaskEndpointCallHandlerAdapter.class),
				instanceOf(OptionalEndpointCallHandlerFactory.class)));
	}
}
