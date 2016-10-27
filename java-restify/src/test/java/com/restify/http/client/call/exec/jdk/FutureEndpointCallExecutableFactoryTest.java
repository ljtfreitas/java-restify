package com.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.SimpleEndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class FutureEndpointCallExecutableFactoryTest {

	private FutureEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new FutureEndpointCallExecutableFactory<>();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsFuture() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("future"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsFuture() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithFutureReturnType() throws Exception {
		EndpointCallExecutable<Future<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("future")));

		String result = "future result";

		Future<String> future = executable.execute(() -> result);

		assertEquals(result, future.get());
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@Test
	public void shouldCreateExecutableWithObjectReturnTypeWhenEndpointMethodReturnTypeIsNotParameterizedFuture() throws Exception {
		EndpointCallExecutable<Future<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFuture")));
		assertEquals(JavaType.of(Object.class), executable.returnType());
	}

	interface SomeType {

		Future<String> future();

		@SuppressWarnings("rawtypes")
		Future dumbFuture();

		String string();
	}
}
