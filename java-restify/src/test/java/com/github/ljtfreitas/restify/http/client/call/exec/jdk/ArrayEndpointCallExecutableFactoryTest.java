package com.github.ljtfreitas.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.contract.metadata.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class ArrayEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCall<String[]> endpointCall;

	private ArrayEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new ArrayEndpointCallExecutableFactory<>();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsCollection() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("array"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotArray() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithArrayReturnType() throws Exception {
		EndpointCallExecutable<String[], String[]> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("array")));

		String[] result = {"result"};

		when(endpointCall.execute())
			.thenReturn(result);

		String[] array = executable.execute(endpointCall, null);

		assertSame(result, array);
		assertEquals(JavaType.of(result.getClass()), executable.returnType());
	}

	interface SomeType {

		String[] array();

		String string();
	}

}
