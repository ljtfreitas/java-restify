package com.restify.http.client.call.exec.guava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.MoreExecutors;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.SimpleEndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class ListenableFutureTaskEndpointCallExecutableFactoryTest {

	private ListenableFutureTaskEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new ListenableFutureTaskEndpointCallExecutableFactory<>(MoreExecutors.newDirectExecutorService());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsListenableFutureTask() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("futureTask"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsListenableFutureTask() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithListenableFutureTaskReturnType() throws Exception {
		EndpointCallExecutable<ListenableFutureTask<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("futureTask")));

		String result = "future result";

		ListenableFuture<String> future = executable.execute(() -> result, null);

		assertEquals(result, future.get());
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@Test
	public void shouldCreateExecutableWithStringReturnTypeWhenEndpointMethodReturnTypeIsNotParameterizedListenableFutureTask() throws Exception {
		EndpointCallExecutable<ListenableFutureTask<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFutureTask")));
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	interface SomeType {

		ListenableFutureTask<String> futureTask();

		@SuppressWarnings("rawtypes")
		ListenableFutureTask dumbFutureTask();

		String string();
	}
}
