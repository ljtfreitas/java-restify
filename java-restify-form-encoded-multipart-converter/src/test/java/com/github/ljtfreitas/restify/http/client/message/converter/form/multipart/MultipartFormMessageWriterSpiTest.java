package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

public class MultipartFormMessageWriterSpiTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<MultipartFormMessageWriter> services = ServiceLoader.load(MultipartFormMessageWriter.class);

		assertThat(services, contains(
				instanceOf(MultipartFormFileObjectMessageWriter.class),
				instanceOf(MultipartFormMapMessageWriter.class),
				instanceOf(MultipartFormObjectMessageWriter.class),
				instanceOf(MultipartFormParametersMessageWriter.class)));
	}
}
