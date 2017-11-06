package com.github.ljtfreitas.restify.http.client.call.exec.rxjava2;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;
import com.github.ljtfreitas.restify.http.spi.ComponentLoader;

public class RxJava2EndpointCallExecutableProviderSpiTest {

	private ComponentLoader loader = new ComponentLoader();

	@SuppressWarnings("unchecked")
	@Test
	public void shouldDiscoveryRxJava2ExecutableProviders() {
		Collection<EndpointCallExecutableProvider> services = loader.load(EndpointCallExecutableProvider.class);

		assertThat(services, contains(
				instanceOf(RxJava2CompletableEndpointCallExecutableFactory.class),
				instanceOf(RxJava2FlowableEndpointCallExecutableFactory.class),
				instanceOf(RxJava2MaybeEndpointCallExecutableFactory.class),
				instanceOf(RxJava2ObservableEndpointCallExecutableFactory.class),
				instanceOf(RxJava2SingleEndpointCallExecutableFactory.class)));
	}
}
