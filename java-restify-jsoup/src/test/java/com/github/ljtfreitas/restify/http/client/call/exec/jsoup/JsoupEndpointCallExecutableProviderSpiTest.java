package com.github.ljtfreitas.restify.http.client.call.exec.jsoup;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableProvider;
import com.github.ljtfreitas.restify.http.spi.ComponentLoader;

public class JsoupEndpointCallExecutableProviderSpiTest {

	private ComponentLoader loader = new ComponentLoader();

	@Test
	public void shouldDiscoveryJsoupExecutableProviders() {
		Collection<EndpointCallExecutableProvider> services = loader.load(EndpointCallExecutableProvider.class);

		assertThat(services, contains(
				instanceOf(JsoupDocumentEndpointCallExecutableFactory.class)));
	}
}
