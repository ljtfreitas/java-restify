package com.github.ljtfreitas.restify.http.client.message.converter.wildcard;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

public class WildcardMessageConverterSpiTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<WildcardMessageConverter> services = ServiceLoader.load(WildcardMessageConverter.class);

		assertThat(services, contains(
				instanceOf(ByteArrayMessageConverter.class),
				instanceOf(InputStreamMessageConverter.class),
				instanceOf(SimpleTextMessageConverter.class)));
	}
}
