package com.restify.http.client.call.exec.guava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.SimpleEndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

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
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsOptional() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithOptionalReturnType() throws Exception {
		EndpointCallExecutable<Optional<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("optional")));

		String result = "result";

		Optional<String> future = executable.execute(() -> result);

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
