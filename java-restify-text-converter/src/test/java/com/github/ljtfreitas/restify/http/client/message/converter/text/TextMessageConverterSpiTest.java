package com.github.ljtfreitas.restify.http.client.message.converter.text;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

public class TextMessageConverterSpiTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<TextMessageConverter> services = ServiceLoader.load(TextMessageConverter.class);

		assertThat(services, contains(
				instanceOf(ScalarMessageConverter.class),
				instanceOf(TextHtmlMessageConverter.class),
				instanceOf(TextPlainMessageConverter.class)));
	}
}
