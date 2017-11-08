package com.github.ljtfreitas.restify.http.client.message.converter.form;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.spi.Provider;

public class FormURLEncodedMessageConverterSpiTest {

	private Provider loader = new Provider();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void shouldDiscoveryGuavaExecutableProviders() {
		Collection<FormURLEncodedMessageConverter> services = loader.all(FormURLEncodedMessageConverter.class);

		assertThat(services, contains(
				instanceOf(FormURLEncodedFormObjectMessageConverter.class),
				instanceOf(FormURLEncodedMapMessageConverter.class),
				instanceOf(FormURLEncodedParametersMessageConverter.class)));
	}
}
