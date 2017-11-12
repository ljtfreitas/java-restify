package com.github.ljtfreitas.restify.http.client.call.exec.jsoup;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;
import com.github.ljtfreitas.restify.http.spi.Provider;

public class JsoupEndpointCallExecutableProviderSpiTest {

	private Provider loader = new Provider();

	@Test
	public void shouldDiscoveryServiceProviders() {
		Collection<EndpointCallExecutableProvider> services = loader.all(EndpointCallExecutableProvider.class);

		assertThat(services, contains(
				instanceOf(JsoupDocumentEndpointCallExecutableFactory.class)));
	}
}
