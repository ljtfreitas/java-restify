package com.github.ljtfreitas.restify.http.client.message.converter.form;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.spi.ComponentLoader;

public class FormURLEncodedMessageConverterSpiTest {

	private ComponentLoader loader = new ComponentLoader();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void shouldDiscoveryGuavaExecutableProviders() {
		Collection<FormURLEncodedMessageConverter> services = loader.load(FormURLEncodedMessageConverter.class);

		assertThat(services, contains(
				instanceOf(FormURLEncodedFormObjectMessageConverter.class),
				instanceOf(FormURLEncodedMapMessageConverter.class),
				instanceOf(FormURLEncodedParametersMessageConverter.class)));
	}
}
