package com.github.ljtfreitas.restify.http.call.handler;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class QueueEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<Collection<String>, Collection<String>> delegate;

	private QueueEndpointCallHandlerAdapter<String> adapter;

	@Before
	public void setup() {
		adapter = new QueueEndpointCallHandlerAdapter<>();

		when(delegate.handle(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.parameterizedType(Collection.class, null, String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsQueue() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("queue"))));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsSubTypeOfQueue() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("deque"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotQueue() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnCollectionParameterizedWithArgumentTypeOfQueue() throws Exception {
		assertEquals(JavaType.parameterizedType(Collection.class, null, String.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("queue"))));
	}

	@Test
	public void shouldReturnCollectionParameterizedWithObjectWhenQueueIsNotParameterized() throws Exception {
		assertEquals(JavaType.parameterizedType(Collection.class, null, Object.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbQueue"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithIteratorReturnType() throws Exception {
		EndpointCallHandler<Queue<String>, Collection<String>> handler = adapter.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("queue")), delegate);

		Collection<String> result = Arrays.asList("result");

		Queue<String> queue = handler.handle(() -> result, null);

		assertEquals(JavaType.parameterizedType(Collection.class, null, String.class), handler.returnType());

		assertThat(queue, contains("result"));
	}

	private interface SomeType {

		Queue<String> queue();

		Deque<String> deque();

		@SuppressWarnings("rawtypes")
		Queue dumbQueue();

		String string();
	}

}
