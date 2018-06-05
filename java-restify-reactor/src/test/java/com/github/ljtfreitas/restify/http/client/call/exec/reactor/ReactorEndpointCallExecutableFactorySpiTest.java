package com.github.ljtfreitas.restify.http.client.call.exec.reactor;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;

@RunWith(MockitoJUnitRunner.class)
public class ReactorEndpointCallExecutableFactorySpiTest {

	@SuppressWarnings("unchecked")
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<EndpointCallExecutableProvider> services = ServiceLoader.load(EndpointCallExecutableProvider.class);

		assertThat(services, contains(
				instanceOf(MonoEndpointCallExecutableFactory.class),
				instanceOf(FluxEndpointCallExecutableFactory.class)));
	}
}
