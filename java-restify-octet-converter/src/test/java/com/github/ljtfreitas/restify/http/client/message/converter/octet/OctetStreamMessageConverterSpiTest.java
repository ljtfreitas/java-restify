package com.github.ljtfreitas.restify.http.client.message.converter.octet;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

public class OctetStreamMessageConverterSpiTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<OctetStreamMessageConverter> services = ServiceLoader.load(OctetStreamMessageConverter.class);

		assertThat(services, contains(
				instanceOf(OctetByteArrayMessageConverter.class),
				instanceOf(OctetInputStreamMessageConverter.class),
				instanceOf(OctetSerializableMessageConverter.class)));
	}
}
