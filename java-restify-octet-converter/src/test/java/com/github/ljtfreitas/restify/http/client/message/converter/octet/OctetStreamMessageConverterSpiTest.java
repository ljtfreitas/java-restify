package com.github.ljtfreitas.restify.http.client.message.converter.octet;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.message.converter.octet.provided.OctetByteArrayMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.octet.provided.OctetInputStreamMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.octet.provided.OctetSerializableMessageConverter;

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
