package com.github.ljtfreitas.restify.http.client.message.converter.form;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

public class FormURLEncodedMessageConverterSpiTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<FormURLEncodedMessageConverter> services = ServiceLoader.load(FormURLEncodedMessageConverter.class);

		assertThat(services, contains(
				instanceOf(FormURLEncodedFormObjectMessageConverter.class),
				instanceOf(FormURLEncodedMapMessageConverter.class),
				instanceOf(FormURLEncodedParametersMessageConverter.class)));
	}
}
