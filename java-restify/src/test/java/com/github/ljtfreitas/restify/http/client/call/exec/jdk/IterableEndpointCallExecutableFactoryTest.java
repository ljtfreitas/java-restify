package com.github.ljtfreitas.restify.http.client.call.exec.jdk;

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
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class IterableEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<Collection<String>, Collection<String>> delegate;

	private IterableEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new IterableEndpointCallExecutableFactory<>();

		when(delegate.execute(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(new SimpleParameterizedType(Collection.class, null, String.class)));
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
		assertEquals(JavaType.of(new SimpleParameterizedType(Collection.class, null, String.class)),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("iterable"))));
	}

	@Test
	public void shouldReturnCollectionParameterizedWithObjectWhenIterableIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(new SimpleParameterizedType(Collection.class, null, Object.class)),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbIterable"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithIteratorReturnType() throws Exception {
		EndpointCallExecutable<Iterable<String>, Collection<String>> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("iterable")), delegate);

		Collection<String> result = Arrays.asList("result");

		Iterable<String> iterable = executable.execute(() -> result, null);

		Iterator<String> iterator = iterable.iterator();

		assertTrue(iterator.hasNext());
		assertEquals("result", iterator.next());
		assertEquals(JavaType.of(new SimpleParameterizedType(Collection.class, null, String.class)), executable.returnType());
	}

	private interface SomeType {

		Iterable<String> iterable();

		@SuppressWarnings("rawtypes")
		Iterable dumbIterable();

		String string();
	}

}
