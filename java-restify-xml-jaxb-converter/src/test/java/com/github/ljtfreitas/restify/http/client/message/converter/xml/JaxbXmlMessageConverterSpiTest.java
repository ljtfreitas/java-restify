package com.github.ljtfreitas.restify.http.client.message.converter.xml;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

public class JaxbXmlMessageConverterSpiTest {

	@SuppressWarnings({ "rawtypes" })
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<XmlMessageConverter> services = ServiceLoader.load(XmlMessageConverter.class);

		assertThat(services, contains(
				instanceOf(JaxbXmlMessageConverter.class)));
	}
}
