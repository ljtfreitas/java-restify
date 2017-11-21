package com.github.ljtfreitas.restify.spi;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

import com.github.ljtfreitas.restify.spi.Provider;

public class ProviderTest {

	@Test
	public void test() {
		Provider loader = new Provider();

		Collection<MyService> services = loader.all(MyService.class);

		assertThat(services, contains(instanceOf(DefaultMyService.class)));
	}
}
