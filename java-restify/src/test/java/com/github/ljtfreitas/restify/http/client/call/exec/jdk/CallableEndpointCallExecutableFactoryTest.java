package com.github.ljtfreitas.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.CallableEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class CallableEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	private CallableEndpointCallExecutableFactory<String, String> factory;

	@Before
	public void setup() {
		factory = new CallableEndpointCallExecutableFactory<>();

		when(delegate.execute(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsCallable() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("callable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotCallable() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfCallable() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("callable"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenCallableIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbCallable"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithCallableReturnType() throws Exception {
		EndpointCallExecutable<Callable<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("callable")), delegate);

		String result = "callable result";

		Callable<String> callable = executable.execute(() -> result, null);

		assertEquals(result, callable.call());
		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(any(), anyVararg());
	}

	interface SomeType {

		Callable<String> callable();

		@SuppressWarnings("rawtypes")
		Callable dumbCallable();

		String string();
	}
}
