package com.restify.http.spring.client.call.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.annotation.AsyncResult;

import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.reflection.JavaType;

public class AsyncResultEndpointCallExecutableFactoryTest {

	private AsyncResultEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new AsyncResultEndpointCallExecutableFactory<>();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsAsyncResult() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("asyncResult"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsAsyncResult() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithAsyncResultReturnType() throws Exception {
		EndpointCallExecutable<AsyncResult<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("asyncResult")));

		String result = "async result";

		AsyncResult<String> asyncResult = executable.execute(() -> result, null);

		assertEquals(result, asyncResult.get());
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@Test
	public void shouldCreateExecutableWithObjectReturnTypeWhenEndpointMethodReturnTypeIsNotParameterizedAsyncResult() throws Exception {
		EndpointCallExecutable<AsyncResult<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("dumbAsyncResult")));
		assertEquals(JavaType.of(Object.class), executable.returnType());
	}

	interface SomeType {

		AsyncResult<String> asyncResult();

		@SuppressWarnings("rawtypes")
		AsyncResult dumbAsyncResult();

		String string();
	}

}
