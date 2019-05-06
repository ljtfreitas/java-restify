package com.github.ljtfreitas.restify.http.client.message.converter.xml;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.message.converter.xml.jaxb.JaxBXmlMessageConverter;

public class JaxBXmlMessageConverterSpiTest {

	@SuppressWarnings({ "rawtypes" })
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<XmlMessageConverter> services = ServiceLoader.load(XmlMessageConverter.class);

		assertThat(services, contains(
				instanceOf(JaxBXmlMessageConverter.class)));
	}
}
