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
				instanceOf(RxJava2FlowableEndpointCallExecutableAdapter.class),
				instanceOf(RxJava2MaybeEndpointCallExecutableAdapter.class),
				instanceOf(RxJava2ObservableEndpointCallExecutableAdapter.class),
				instanceOf(RxJava2SingleEndpointCallExecutableAdapter.class)));
	}
}
