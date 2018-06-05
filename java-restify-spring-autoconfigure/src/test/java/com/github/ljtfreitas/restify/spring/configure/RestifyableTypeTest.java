package com.github.ljtfreitas.restify.spring.configure;

import static org.junit.Assert.*;

import org.junit.Test;

public class RestifyableTypeTest {

	@Test
	public void shouldGetTypeNameFromClassName() {
		RestifyableType type = new RestifyableType(MyApi.class);

		assertEquals("my-api", type.name());
	}

	@Test
	public void shouldGetTypeNameFromAnnotation() {
		RestifyableType type = new RestifyableType(MyWhateverApi.class);

		assertEquals("my-whatever-api", type.name());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenTypeHasNotTheAnnotation() {
		new RestifyableType(MyDumbApi.class);
	}

	@Restifyable
	private interface MyApi {
	}

	@Restifyable(name = "myWhateverApi", description = "My Whatever Api", endpoint = "http://my.whatever.com/api")
	private interface MyWhateverApi {
	}

	private interface MyDumbApi {
	}
}
