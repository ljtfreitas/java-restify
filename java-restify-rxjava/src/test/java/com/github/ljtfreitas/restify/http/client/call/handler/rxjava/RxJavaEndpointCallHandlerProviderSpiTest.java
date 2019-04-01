package com.github.ljtfreitas.restify.http.client.call.handler.rxjava;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerProvider;

public class RxJavaEndpointCallHandlerProviderSpiTest {

	@SuppressWarnings("unchecked")
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<EndpointCallHandlerProvider> services = ServiceLoader.load(EndpointCallHandlerProvider.class);

		assertThat(services, contains(
				instanceOf(RxJavaCompletableEndpointCallHandlerFactory.class),
				instanceOf(RxJavaObservableEndpointCallHandlerAdapter.class),
				instanceOf(RxJavaSingleEndpointCallHandlerAdapter.class)));
	}
}
