package com.github.ljtfreitas.restify.http.client.call.handler.rxjava2;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerProvider;

public class RxJava2EndpointCallHandlerProviderSpiTest {

	@SuppressWarnings("unchecked")
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<EndpointCallHandlerProvider> services = ServiceLoader.load(EndpointCallHandlerProvider.class);

		assertThat(services, contains(
				instanceOf(RxJava2CompletableEndpointCallHandlerFactory.class),
				instanceOf(RxJava2FlowableEndpointCallHandlerAdapter.class),
				instanceOf(RxJava2MaybeEndpointCallHandlerAdapter.class),
				instanceOf(RxJava2ObservableEndpointCallHandlerAdapter.class),
				instanceOf(RxJava2SingleEndpointCallHandlerAdapter.class)));
	}
}
