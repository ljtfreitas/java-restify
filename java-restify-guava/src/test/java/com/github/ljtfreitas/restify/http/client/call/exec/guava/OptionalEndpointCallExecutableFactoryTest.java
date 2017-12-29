package com.github.ljtfreitas.restify.http.client.call.exec.guava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.guava.OptionalEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.google.common.base.Optional;

public class OptionalEndpointCallExecutableFactoryTest {

	private OptionalEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new OptionalEndpointCallExecutableFactory<>();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsOptional() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("optional"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotOptional() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithOptionalReturnType() throws Exception {
		EndpointCallExecutable<Optional<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("optional")));

		String result = "result";

		Optional<String> future = executable.execute(() -> result, null);

		assertEquals(result, future.get());
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@Test
	public void shouldCreateExecutableWithObjectReturnTypeWhenEndpointMethodReturnTypeIsNotParameterizedOptional() throws Exception {
		EndpointCallExecutable<Optional<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("dumbOptional")));
		assertEquals(JavaType.of(Object.class), executable.returnType());
	}

	interface SomeType {

		Optional<String> optional();

		@SuppressWarnings("rawtypes")
		Optional dumbOptional();

		String string();
	}

}
