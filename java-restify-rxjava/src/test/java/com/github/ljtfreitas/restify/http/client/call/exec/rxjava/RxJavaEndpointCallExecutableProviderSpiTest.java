package com.github.ljtfreitas.restify.http.client.call.exec.rxjava;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;
import com.github.ljtfreitas.restify.http.spi.Provider;

public class RxJavaEndpointCallExecutableProviderSpiTest {

	private Provider loader = new Provider();

	@SuppressWarnings("unchecked")
	@Test
	public void shouldDiscoveryRxJavaExecutableProviders() {
		Collection<EndpointCallExecutableProvider> services = loader.all(EndpointCallExecutableProvider.class);

		assertThat(services, contains(
				instanceOf(RxJavaCompletableEndpointCallExecutableFactory.class),
				instanceOf(RxJavaObservableEndpointCallExecutableFactory.class),
				instanceOf(RxJavaSingleEndpointCallExecutableFactory.class)));
	}
}
