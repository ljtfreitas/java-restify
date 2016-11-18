package com.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.SimpleEndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class OptionalEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCall<String> endpointCall;

	private OptionalEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new OptionalEndpointCallExecutableFactory<>();
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
		EndpointCallExecutable<Optional<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("optional")));

		String result = "result";

		when(endpointCall.execute())
			.thenReturn(result);

		Optional<String> future = executable.execute(endpointCall, null);

		assertEquals(result, future.get());
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@Test
	public void shouldCreateExecutableWithObjectReturnTypeWhenEndpointMethodReturnTypeIsNotParameterizedOptional() throws Exception {
		EndpointCallExecutable<Optional<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("dumbOptional")));
		assertEquals(JavaType.of(Object.class), executable.returnType());
	}

	interface SomeType {

		Optional<String> optional();

		@SuppressWarnings("rawtypes")
		Optional dumbOptional();

		String string();
	}

}
