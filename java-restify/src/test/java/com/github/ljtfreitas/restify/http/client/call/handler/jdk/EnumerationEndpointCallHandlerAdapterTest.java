package com.github.ljtfreitas.restify.http.client.call.handler.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class EnumerationEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<Collection<String>, Collection<String>> delegate;

	private EnumerationEndpointCallHandlerAdapter<String> adapter;

	@Before
	public void setup() {
		adapter = new EnumerationEndpointCallHandlerAdapter<>();

		when(delegate.handle(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(new SimpleParameterizedType(Collection.class, null, String.class)));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsEnumeration() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("enumeration"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotEnumeration() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnCollectionParameterizedWithArgumentTypeOfEnumeration() throws Exception {
		assertEquals(JavaType.of(new SimpleParameterizedType(Collection.class, null, String.class)), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("enumeration"))));
	}

	@Test
	public void shouldReturnCollectionParameterizedWithObjectWhenEnumerationIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(new SimpleParameterizedType(Collection.class, null, Object.class)), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbEnumeration"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithEnumerationReturnType() throws Exception {
		EndpointCallHandler<Enumeration<String>, Collection<String>> handler = adapter.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("enumeration")), delegate);

		Collection<String> result = Arrays.asList("result");

		Enumeration<String> enumeration = handler.handle(() -> result, null);

		assertTrue(enumeration.hasMoreElements());
		assertEquals("result", enumeration.nextElement());
		assertEquals(JavaType.of(new SimpleParameterizedType(Collection.class, null, String.class)), handler.returnType());
	}

	private interface SomeType {

		Enumeration<String> enumeration();

		@SuppressWarnings("rawtypes")
		Enumeration dumbEnumeration();

		String string();
	}

}
