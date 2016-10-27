package com.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.SimpleEndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class CompletableFutureEndpointCallExecutableFactoryTest {

	private CompletableFutureEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new CompletableFutureEndpointCallExecutableFactory<>(r -> r.run());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsCompletableFuture() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("future"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsCompletableFuture() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithCompletableFutureReturnType() throws Exception {
		EndpointCallExecutable<CompletableFuture<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("future")));

		String result = "future result";

		CompletableFuture<String> future = executable.execute(() -> result);

		assertEquals(result, future.get());
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@Test
	public void shouldCreateExecutableWithObjectReturnTypeWhenEndpointMethodReturnTypeIsNotParameterizedCompletableFuture() throws Exception {
		EndpointCallExecutable<CompletableFuture<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFuture")));
		assertEquals(JavaType.of(Object.class), executable.returnType());
	}

	interface SomeType {

		CompletableFuture<String> future();

		@SuppressWarnings("rawtypes")
		CompletableFuture dumbFuture();

		String string();
	}
}
