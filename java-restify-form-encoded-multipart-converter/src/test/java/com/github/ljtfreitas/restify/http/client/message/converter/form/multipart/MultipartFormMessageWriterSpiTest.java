package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.spi.Provider;

public class MultipartFormMessageWriterSpiTest {

	private Provider loader = new Provider();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void shouldDiscoveryGuavaExecutableProviders() {
		Collection<MultipartFormMessageWriter> services = loader.all(MultipartFormMessageWriter.class);

		assertThat(services, contains(
				instanceOf(MultipartFormFileObjectMessageWriter.class),
				instanceOf(MultipartFormMapMessageWriter.class),
				instanceOf(MultipartFormObjectMessageWriter.class),
				instanceOf(MultipartFormParametersMessageWriter.class)));
	}
}
