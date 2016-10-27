package com.restify.http.client.call.exec.guava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.SimpleEndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class ListenableFutureEndpointCallExecutableFactoryTest {

	private ListenableFutureEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new ListenableFutureEndpointCallExecutableFactory<>(MoreExecutors.newDirectExecutorService());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsListenableFuture() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("future"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsListenableFuture() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithListenableReturnType() throws Exception {
		EndpointCallExecutable<ListenableFuture<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("future")));

		String result = "future result";

		ListenableFuture<String> future = executable.execute(() -> result);

		assertEquals(result, future.get());
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@Test
	public void shouldCreateExecutableWithStringReturnTypeWhenEndpointMethodReturnTypeIsNotParameterizedListenableFuture() throws Exception {
		EndpointCallExecutable<ListenableFuture<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFuture")));
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	interface SomeType {

		ListenableFuture<String> future();

		@SuppressWarnings("rawtypes")
		ListenableFuture dumbFuture();

		String string();
	}
}
