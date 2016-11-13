package com.restify.http.spring.client.call.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.concurrent.ListenableFutureTask;

import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.reflection.JavaType;

public class ListenableFutureTaskEndpointCallExecutableFactoryTest {

	private ListenableFutureTaskEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new ListenableFutureTaskEndpointCallExecutableFactory<>();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsListenableFutureTask() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("future"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsListenableFutureTask() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithListenableFutureTaskReturnType() throws Exception {
		EndpointCallExecutable<ListenableFutureTask<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("future")));

		String result = "future result";

		ListenableFutureTask<String> future = executable.execute(() -> result, null);

		assertEquals(result, future.get());
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@Test
	public void shouldCreateExecutableWithObjectReturnTypeWhenEndpointMethodReturnTypeIsNotParameterizedListenableFutureTask() throws Exception {
		EndpointCallExecutable<ListenableFutureTask<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFuture")));
		assertEquals(JavaType.of(Object.class), executable.returnType());
	}

	interface SomeType {

		ListenableFutureTask<String> future();

		@SuppressWarnings("rawtypes")
		ListenableFutureTask dumbFuture();

		String string();
	}
}
