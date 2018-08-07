package com.github.ljtfreitas.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

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
public class ListIteratorEndpointCallExecutableAdapterTest {

	@Mock
	private EndpointCallExecutable<List<String>, List<String>> delegate;

	private ListIteratorEndpointCallExecutableAdapter<String> adapter;

	@Before
	public void setup() {
		adapter = new ListIteratorEndpointCallExecutableAdapter<>();

		when(delegate.execute(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(new SimpleParameterizedType(List.class, null, String.class)));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsListIterator() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("listIterator"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotListIterator() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnListParameterizedWithArgumentTypeOfListIterator() throws Exception {
		assertEquals(JavaType.of(new SimpleParameterizedType(List.class, null, String.class)),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("listIterator"))));
	}

	@Test
	public void shouldReturnListParameterizedWithObjectWhenListIteratorIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(new SimpleParameterizedType(List.class, null, Object.class)),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbListIterator"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithListIteratorReturnType() throws Exception {
		EndpointCallExecutable<ListIterator<String>, List<String>> executable = adapter.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("listIterator")), delegate);

		List<String> result = Arrays.asList("result");

		ListIterator<String> ListIterator = executable.execute(() -> result, null);

		assertTrue(ListIterator.hasNext());
		assertEquals("result", ListIterator.next());
		assertEquals(JavaType.of(new SimpleParameterizedType(List.class, null, String.class)), executable.returnType());
	}

	private interface SomeType {

		ListIterator<String> listIterator();

		@SuppressWarnings("rawtypes")
		ListIterator dumbListIterator();

		String string();
	}

}
