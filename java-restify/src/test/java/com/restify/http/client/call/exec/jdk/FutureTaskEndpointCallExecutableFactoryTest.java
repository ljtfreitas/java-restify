package com.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.FutureTask;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.SimpleEndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class FutureTaskEndpointCallExecutableFactoryTest {

	private FutureTaskEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new FutureTaskEndpointCallExecutableFactory<>();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsFutureTask() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("futureTask"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsFutureTask() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithFutureTaskReturnType() throws Exception {
		EndpointCallExecutable<FutureTask<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("futureTask")));

		String result = "future task result";

		FutureTask<String> future = executable.execute(() -> result);

		assertEquals(result, future.get());
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@Test
	public void shouldCreateExecutableWithObjectReturnTypeWhenEndpointMethodReturnTypeIsNotParameterizedFutureTask() throws Exception {
		EndpointCallExecutable<FutureTask<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFutureTask")));
		assertEquals(JavaType.of(Object.class), executable.returnType());
	}

	interface SomeType {

		FutureTask<String> futureTask();

		@SuppressWarnings("rawtypes")
		FutureTask dumbFutureTask();

		String string();
	}
}
