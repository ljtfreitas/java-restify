package com.github.ljtfreitas.restify.http.client.message.converter.json;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.message.converter.json.jsonb.JsonBMessageConverter;

public class JsonBMessageConverterSpiTest {

	@SuppressWarnings({ "rawtypes" })
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<JsonMessageConverter> services = ServiceLoader.load(JsonMessageConverter.class);

		assertThat(services, contains(
				instanceOf(JsonBMessageConverter.class)));
	}
}
