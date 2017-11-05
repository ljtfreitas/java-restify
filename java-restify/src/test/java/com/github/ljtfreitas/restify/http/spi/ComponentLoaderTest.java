package com.github.ljtfreitas.restify.http.spi;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

public class ComponentLoaderTest {

	@Test
	public void test() {
		ComponentLoader loader = new ComponentLoader();

		Collection<MyService> services = loader.load(MyService.class);

		assertThat(services, contains(instanceOf(DefaultMyService.class)));
	}
}
