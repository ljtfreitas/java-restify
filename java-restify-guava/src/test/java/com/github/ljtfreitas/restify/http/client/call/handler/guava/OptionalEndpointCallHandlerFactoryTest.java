package com.github.ljtfreitas.restify.http.client.call.handler.guava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.google.common.base.Optional;

public class OptionalEndpointCallHandlerFactoryTest {

	private OptionalEndpointCallHandlerFactory<String> factory;

	@Before
	public void setup() {
		factory = new OptionalEndpointCallHandlerFactory<>();
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
		EndpointCallHandler<Optional<String>, String> handler = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("optional")));

		String result = "result";

		Optional<String> future = handler.handle(() -> result, null);

		assertEquals(result, future.get());
		assertEquals(JavaType.of(String.class), handler.returnType());
	}

	@Test
	public void shouldCreateExecutableWithObjectReturnTypeWhenEndpointMethodReturnTypeIsNotParameterizedOptional() throws Exception {
		EndpointCallHandler<Optional<String>, String> handler = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("dumbOptional")));
		assertEquals(JavaType.of(Object.class), handler.returnType());
	}

	interface SomeType {

		Optional<String> optional();

		@SuppressWarnings("rawtypes")
		Optional dumbOptional();

		String string();
	}

}
