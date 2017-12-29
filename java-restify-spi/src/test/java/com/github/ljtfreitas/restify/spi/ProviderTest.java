package com.github.ljtfreitas.restify.spi;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Optional;

import org.junit.Test;

import com.github.ljtfreitas.restify.spi.Provider;

public class ProviderTest {

	@SuppressWarnings("unchecked")
	@Test
	public void shouldLoadAllImplementationsOfService() {
		Provider loader = new Provider();

		Collection<MyService> services = loader.all(MyService.class);

		assertThat(services, contains(instanceOf(DefaultMyService.class), instanceOf(OtherMyService.class)));
	}

	@Test
	public void shouldLoadFirstImplementationsOfService() {
		Provider loader = new Provider();

		Optional<MyService> service = loader.single(MyService.class);

		assertTrue(service.isPresent());
		assertThat(service.get(), instanceOf(DefaultMyService.class));
	}
}
