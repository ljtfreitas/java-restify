package com.github.ljtfreitas.restify.http.client.call.exec.rxjava2;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;

public class RxJava2EndpointCallExecutableProviderSpiTest {

	@SuppressWarnings("unchecked")
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<EndpointCallExecutableProvider> services = ServiceLoader.load(EndpointCallExecutableProvider.class);

		assertThat(services, contains(
				instanceOf(RxJava2CompletableEndpointCallExecutableFactory.class),
				instanceOf(RxJava2FlowableEndpointCallExecutableFactory.class),
				instanceOf(RxJava2MaybeEndpointCallExecutableFactory.class),
				instanceOf(RxJava2ObservableEndpointCallExecutableFactory.class),
				instanceOf(RxJava2SingleEndpointCallExecutableFactory.class)));
	}
}
