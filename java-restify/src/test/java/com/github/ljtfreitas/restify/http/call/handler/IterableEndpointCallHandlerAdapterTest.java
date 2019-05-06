package com.github.ljtfreitas.restify.http.call.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class IterableEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<Collection<String>, Collection<String>> delegate;

	private IterableEndpointCallHandlerAdapter<String> factory;

	@Before
	public void setup() {
		factory = new IterableEndpointCallHandlerAdapter<>();

		when(delegate.handle(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.parameterizedType(Collection.class, null, String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsIterable() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("iterable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotIterable() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnCollectionParameterizedWithArgumentTypeOfIterable() throws Exception {
		assertEquals(JavaType.parameterizedType(Collection.class, null, String.class),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("iterable"))));
	}

	@Test
	public void shouldReturnCollectionParameterizedWithObjectWhenIterableIsNotParameterized() throws Exception {
		assertEquals(JavaType.parameterizedType(Collection.class, null, Object.class),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbIterable"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithIteratorReturnType() throws Exception {
		EndpointCallHandler<Iterable<String>, Collection<String>> handler = factory.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("iterable")), delegate);

		Collection<String> result = Arrays.asList("result");

		Iterable<String> iterable = handler.handle(() -> result, null);

		Iterator<String> iterator = iterable.iterator();

		assertTrue(iterator.hasNext());
		assertEquals("result", iterator.next());
		assertEquals(JavaType.parameterizedType(Collection.class, null, String.class), handler.returnType());
	}

	private interface SomeType {

		Iterable<String> iterable();

		@SuppressWarnings("rawtypes")
		Iterable dumbIterable();

		String string();
	}

}
