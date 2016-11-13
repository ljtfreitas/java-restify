package com.restify.http.spring.client.call.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.request.async.DeferredResult;

import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.reflection.JavaType;

public class DeferredResultEndpointCallExecutableFactoryTest {

	private DeferredResultEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new DeferredResultEndpointCallExecutableFactory<>(r -> r.run());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsDeferredResult() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("deferredResult"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsDeferredResult() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithDeferredResultReturnType() throws Exception {
		EndpointCallExecutable<DeferredResult<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("deferredResult")));

		String result = "deferred result";

		DeferredResult<String> deferredResult = executable.execute(() -> result, null);

		assertEquals(result, deferredResult.getResult());
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@Test
	public void shouldCreateExecutableWithObjectReturnTypeWhenEndpointMethodReturnTypeIsNotParameterizedDeferredResult() throws Exception {
		EndpointCallExecutable<DeferredResult<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("dumbDeferredResult")));
		assertEquals(JavaType.of(Object.class), executable.returnType());
	}

	interface SomeType {

		DeferredResult<String> deferredResult();

		@SuppressWarnings("rawtypes")
		DeferredResult dumbDeferredResult();

		String string();
	}

}
