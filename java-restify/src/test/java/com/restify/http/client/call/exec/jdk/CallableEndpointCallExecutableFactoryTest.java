package com.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.SimpleEndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class CallableEndpointCallExecutableFactoryTest {

	private CallableEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new CallableEndpointCallExecutableFactory<>();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsCallable() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("callable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsCallable() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithCallableReturnType() throws Exception {
		EndpointCallExecutable<Callable<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("callable")));

		String result = "callable result";

		Callable<String> callable = executable.execute(() -> result);

		assertEquals(result, callable.call());
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@Test
	public void shouldCreateExecutableWithObjectReturnTypeWhenEndpointMethodReturnTypeIsNotParameterizedCallable() throws Exception {
		EndpointCallExecutable<Callable<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("dumbCallable")));
		assertEquals(JavaType.of(Object.class), executable.returnType());
	}

	interface SomeType {

		Callable<String> callable();

		@SuppressWarnings("rawtypes")
		Callable dumbCallable();

		String string();
	}
}
