package com.github.ljtfreitas.restify.http.client.call.handler.reactor;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerProvider;

@RunWith(MockitoJUnitRunner.class)
public class ReactorEndpointCallHandlerProviderSpiTest {

	@SuppressWarnings("unchecked")
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<EndpointCallHandlerProvider> services = ServiceLoader.load(EndpointCallHandlerProvider.class);

		assertThat(services, contains(
				instanceOf(MonoEndpointCallHandlerAdapter.class),
				instanceOf(FluxEndpointCallHandlerAdapter.class)));
	}
}
